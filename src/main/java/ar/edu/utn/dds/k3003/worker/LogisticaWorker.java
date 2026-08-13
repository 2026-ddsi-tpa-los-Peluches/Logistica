package ar.edu.utn.dds.k3003.worker;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionMensajeDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.componentes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.componentes.LogisticaClient;
import ar.edu.utn.dds.k3003.worker.model.AlgoritmoAsignacion;
import ar.edu.utn.dds.k3003.worker.model.AlgoritmoFactory;
import ar.edu.utn.dds.k3003.worker.model.NecesidadLogistica;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

public class LogisticaWorker extends DefaultConsumer {

    private final String queueName;
    private final DonadoresYEntidadesClient donadoresEntidadesClient;
    private final LogisticaClient logisticaClient;
    private final ObjectMapper objectMapper;

    public LogisticaWorker(Channel channel,
                           String queueName,
                           DonadoresYEntidadesClient donadoresEntidadesClient,
                           LogisticaClient logisticaClient) {
        super(channel);
        this.queueName = queueName;
        this.donadoresEntidadesClient = donadoresEntidadesClient;
        this.logisticaClient = logisticaClient;
        this.objectMapper = new ObjectMapper();
    }

    public void init() throws IOException {
        // Declarar la cola (durable: false, exclusive: false, autoDelete: false)
        this.getChannel().queueDeclare(this.queueName, false, false, false, null);

        // Empezar a escuchar (autoAck: false para confirmar manualmente)
        this.getChannel().basicConsume(this.queueName, false, this);
        System.out.println("👷 Worker de Logística escuchando en la cola: " + queueName);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope,
                               AMQP.BasicProperties properties, byte[] body) throws IOException {

        try {
            String json = new String(body, StandardCharsets.UTF_8);
            System.out.println("📦 Worker procesando donación: " + json);

            // 1. Deserializar el JSON recibido
            DonacionMensajeDTO donacion = objectMapper.readValue(json, DonacionMensajeDTO.class);

            // 2. Consultar necesidades mediante HTTP a "Donadores y Entidades"
            List<NecesidadLogistica> necesidades = donadoresEntidadesClient.obtenerNecesidadesInsatisfechasDe(donacion.productoID())
                    .stream().map(this::toDomain).toList();


            if (necesidades.isEmpty()) {
                // Guardar en stock en Logística haciendo un POST /stock
                logisticaClient.guardarEnStock(donacion);
            } else {
                // 3. Ejecutar el Matchmaking en memoria (adaptado a los tiposDTO actuales)
                NecesidadLogistica elegida = ejecutarMatchmaking(
                        donacion.tipoAlgoritmo(),
                        donacion.cantidadDonada(),
                        necesidades
                );

                NecesidadMaterialDTO necesidad = donadoresEntidadesClient.obtenerNecesidadPorId(elegida.getId());


                logisticaClient.asignarProductoAEntidad(necesidad);

                donadoresEntidadesClient.satisfacerNecesidad(
                        elegida.getId(),
                        donacion.cantidadDonada()
                );
            }

            // 5. Confirmar recepción a RabbitMQ SOLAMENTE si todo salió bien
            this.getChannel().basicAck(envelope.getDeliveryTag(), false);

        } catch (Exception e) {
            System.err.println("Error procesando el mensaje: " + e.getMessage());
            e.printStackTrace();

            // Opcional: rechazar el mensaje para no reintentar infinitamente en bucle (requeue = false)
            // this.getChannel().basicNack(envelope.getDeliveryTag(), false, false);
        }
    }
    private NecesidadLogistica ejecutarMatchmaking(
            TipoAlgoritmoEnum tipoAlgoritmo,
            int cantidadDonada,
            List<NecesidadLogistica> necesidadesLogistica) {


        if (cantidadDonada < 0) {
            throw new IllegalArgumentException("no dona nada y hasta roba");
        }

        if (necesidadesLogistica == null || necesidadesLogistica.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades");
        }


        AlgoritmoAsignacion algoritmo =
                AlgoritmoFactory.crear(tipoAlgoritmo);


        NecesidadLogistica elegida = algoritmo.elegir(
                necesidadesLogistica,
                cantidadDonada
        );

        if (elegida == null) {
            throw new NoSuchElementException(
                    "No se pudo asignar necesidad"
            );
        }
        return elegida;

    }

    private NecesidadLogistica toDomain(NecesidadMaterialDTO dto){
        return new NecesidadLogistica(
                dto.id(),
                dto.entidadID(),
                dto.nivelDeUrgencia(),
                dto.cantidadObjetivo(),
                dto.cantidadRecibida()

        );
    }


    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        // 1. Cargar el archivo application.properties manualmente
        Properties props = new Properties();
        try (InputStream input = LogisticaWorker.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("No se encontró el archivo application.properties");
            }
            props.load(input);
        }

        // 2. Extraer las propiedades
        String queueHost = props.getProperty("queue.host");
        String queueUsername = props.getProperty("queue.username");
        String queuePassword = props.getProperty("queue.password");
        String queueName = props.getProperty("queue.name");

        String urlDonadoresYEntidades = props.getProperty("url.donadoresYEntidades");
        String urlLogistica = props.getProperty("url.logistica");

        DonadoresYEntidadesClient donadoresClient = new DonadoresYEntidadesClient(urlDonadoresYEntidades);
        LogisticaClient logisticaClient = new LogisticaClient(urlLogistica);

        // 3. Configurar la conexión con CloudAMQP
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(queueHost);
        factory.setUsername(queueUsername);
        factory.setPassword(queuePassword);
        factory.setVirtualHost(queueUsername);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        LogisticaWorker worker = new LogisticaWorker(channel, queueName, donadoresClient, logisticaClient);
        worker.init();


    }
}
