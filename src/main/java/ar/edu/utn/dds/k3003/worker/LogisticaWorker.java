package ar.edu.utn.dds.k3003.worker;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionMensajeDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.componentes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.componentes.LogisticaClient;
import ar.edu.utn.dds.k3003.worker.model.AlgoritmoAsignacion;
import ar.edu.utn.dds.k3003.worker.model.AlgoritmoFactory;
import ar.edu.utn.dds.k3003.worker.model.NecesidadLogistica;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

@Service // 🟢 Ahora Spring sí puede administrarlo porque ya conoce las dependencias
public class LogisticaWorker extends DefaultConsumer {

    private final String queueName;
    private final DonadoresYEntidadesClient donadoresEntidadesClient;
    private final LogisticaClient logisticaClient;
    private final ObjectMapper objectMapper;

    public LogisticaWorker(Channel channel,
                           @Value("${queue.name}") String queueName, // 🟢 Spring inyecta la propiedad del application.properties
                           DonadoresYEntidadesClient donadoresEntidadesClient,
                           LogisticaClient logisticaClient) {
        super(channel);
        this.queueName = queueName;
        this.donadoresEntidadesClient = donadoresEntidadesClient;
        this.logisticaClient = logisticaClient;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct // 🟢 Se ejecuta automáticamente apenas Spring termina de inicializar
    public void init() throws IOException {
        this.getChannel().queueDeclare(this.queueName, false, false, false, null);
        this.getChannel().basicConsume(this.queueName, false, this);
        System.out.println("👷 Worker de Logística escuchando en la cola: " + queueName);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope,
                               AMQP.BasicProperties properties, byte[] body) throws IOException {

        try {
            String json = new String(body, StandardCharsets.UTF_8);
            System.out.println("📦 Worker procesando donación: " + json);

            DonacionMensajeDTO donacion = objectMapper.readValue(json, DonacionMensajeDTO.class);

            List<NecesidadLogistica> necesidades = donadoresEntidadesClient.obtenerNecesidadesInsatisfechasDe(donacion.productoID())
                    .stream().map(this::toDomain).toList();


            List<NecesidadLogistica> necesidadesAplicables =
                    necesidades.stream()
                .filter(n ->
                        this.esNecesidadAplicable(
                                n,
                                donacion.cantidadDonada()
                        )
                )
                .toList();

            if (necesidadesAplicables.isEmpty()) {
                logisticaClient.guardarEnStock(donacion);
            } else {
                NecesidadLogistica elegida = ejecutarMatchmaking(
                        donacion.tipoAlgoritmo(),
                        donacion.cantidadDonada(),
                        necesidadesAplicables
                );
                AsignacionDTO asignacionDTO = logisticaClient.asignarDesdeDonacion(donacion, elegida.getId(), elegida.getCantidadFaltante());

                donadoresEntidadesClient.satisfacerNecesidad(
                        elegida.getId(),
                        asignacionDTO.cantidad()
                );
            }
            

            this.getChannel().basicAck(envelope.getDeliveryTag(), false);

        } catch (Exception e) {
            System.err.println("Error procesando el mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean esNecesidadAplicable(NecesidadLogistica necesidad, Integer cantidadDonada) {
        if(necesidad.getTipo() == TipoNecesidadMaterialEnum.EXTRAORDINARIA) return true;
        else if (necesidad.getTipo() == TipoNecesidadMaterialEnum.RECURRENTE) {
            //si lo que necesito es menos o a lo sumo igual de lo que me donaron
            return (necesidad.getCantidadObjetivo() - necesidad.getCantidadRecibida()) <= cantidadDonada;
            //capaz estaria bueno que desde donadores y entidades se haga esta logica de
            // cantidad no cubierta o actual - cantidad faltante
        }
        return true;

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

        AlgoritmoAsignacion algoritmo = AlgoritmoFactory.crear(tipoAlgoritmo);

        NecesidadLogistica elegida = algoritmo.elegir(
                necesidadesLogistica,
                cantidadDonada
        );

        if (elegida == null) {
            throw new NoSuchElementException("No se pudo asignar necesidad");
        }
        return elegida;
    }

    private NecesidadLogistica toDomain(NecesidadMaterialDTO dto){
        return new NecesidadLogistica(
                dto.id(),
                dto.entidadID(),
                dto.nivelDeUrgencia(),
                dto.cantidadObjetivo(),
                dto.cantidadRecibida(),
                dto.tipo()
        );
    }
}