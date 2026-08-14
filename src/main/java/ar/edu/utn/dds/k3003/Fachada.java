package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionMensajeDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.componentes.DonacionesClient;
import ar.edu.utn.dds.k3003.componentes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.repositories.asignaciones.AsignacionesRepository;
import ar.edu.utn.dds.k3003.repositories.depositos.DepositosRepository;
import ar.edu.utn.dds.k3003.repositories.paquetes.PaquetesRepository;
import ar.edu.utn.dds.k3003.worker.model.AlgoritmoAsignacion;
import ar.edu.utn.dds.k3003.worker.model.AlgoritmoFactory;
import ar.edu.utn.dds.k3003.worker.model.NecesidadLogistica;
import ar.edu.utn.dds.k3003.worker.model.NecesidadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class Fachada implements FachadaLogistica {

    // CLIENTS
    private final DonadoresYEntidadesClient donadoresYEntidadesClient;
    private final DonacionesClient donacionesClient;

    // METRICAS (DataDog / Micrometer)
    private final Counter depositosCreadosCounter;
    private final Counter paquetesCreadosCounter;              // Métrica 1
    private final Counter cantidadDonadaTotalCounter;           // Métrica 2
    private final Counter asignacionesMatchmakingCounter;       // Métrica 3 (Tag: matchmaking)
    private final Counter asignacionesSolicitudExternaCounter;   // Métrica 3 (Tag: solicitud_externa)
    private final DistributionSummary tamanioAsignacionSummary; // Métrica 4


    // REPOS
    private final DepositosRepository depositoRepo;
    private final AsignacionesRepository asignacionRepo;
    private final PaquetesRepository paqueteRepo;

    @Autowired
    public Fachada(
            MeterRegistry meterRegistry,
            DonadoresYEntidadesClient donadoresYEntidadesClient,
            DonacionesClient donacionesClient,
            DepositosRepository depositoRepo,
            AsignacionesRepository asignacionRepo,
            PaquetesRepository paqueteRepo
    ) {
        this.depositosCreadosCounter = Counter.builder("deposito.creados")
                .description("Deposito creado")
                .register(meterRegistry);

        // --- INICIALIZACIÓN DE NUEVAS MÉTRICAS ---
        // 1. Contador de paquetes creados
        this.paquetesCreadosCounter = Counter.builder("logistica.paquetes.creados")
                .description("Cantidad total de paquetes de donación creados")
                .register(meterRegistry);

        // 2. Contador de unidades físicas donadas acumuladas
        this.cantidadDonadaTotalCounter = Counter.builder("logistica.donaciones.cantidad.total")
                .description("Suma total de items/unidades donadas que ingresan al sistema")
                .register(meterRegistry);

        // 3. Contadores de Asignaciones con Tags para DataDog
        this.asignacionesMatchmakingCounter = Counter.builder("logistica.asignaciones.creadas")
                .tag("origen", "matchmaking")
                .description("Cantidad de asignaciones de paquetes via Algoritmo Matchmaking")
                .register(meterRegistry);

        this.asignacionesSolicitudExternaCounter = Counter.builder("logistica.asignaciones.creadas")
                .tag("origen", "solicitud_externa")
                .description("Cantidad de asignaciones de paquetes via Solicitud Externa")
                .register(meterRegistry);

        // 4. Resumen de distribución del volumen asignado por operación
        this.tamanioAsignacionSummary = DistributionSummary.builder("logistica.asignacion.cantidad.unidades")
                .description("Distribución de la cantidad de unidades asignadas por operación")
                .baseUnit("unidades")
                .register(meterRegistry);

        this.donadoresYEntidadesClient = donadoresYEntidadesClient;
        this.donacionesClient = donacionesClient;

        this.depositoRepo = depositoRepo;
        this.asignacionRepo = asignacionRepo;
        this.paqueteRepo = paqueteRepo;
    }

    @Override
    public DepositoDTO agregarDeposito(DepositoDTO depositoDTO) {
        if (depositoDTO == null || depositoDTO.id() != null) {
            throw new IllegalArgumentException("Deposito inválido");
        }

        Deposito deposito = new Deposito(
                depositoDTO.algoritmo(),
                depositoDTO.nombre(),
                depositoDTO.direccion(),
                depositoDTO.capacidadMaxima()
        );

        // metrica deposito creado
        this.depositosCreadosCounter.increment();

        Deposito depositoConId = depositoRepo.save(deposito);

        return toDTO(depositoConId);
    }

    @Override
    public DepositoDTO buscarDepositoPorID(Integer id) throws NoSuchElementException {
        Deposito deposito = depositoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Depósito no encontrado: " + id));
        return toDTO(deposito);
    }

    public List<DepositoDTO> obtenerDepositos(){
        return this.depositoRepo.findAll().stream().map(this::toDTO).toList();
    }

    public List<AsignacionDTO> obtenerAsignaciones(){
        return this.asignacionRepo.findAll().stream().map(this::toDTO).toList();
    }

    public List<PaqueteDTO> obtenerPaquetes(){
        return this.paqueteRepo.findAll().stream().map(this::toDTO).toList();
    }

    public void borrarDepositoPorID(Integer id) {
        Deposito deposito = depositoRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado: " + id));

        depositoRepo.delete(deposito);
    }

    @Override
    public AsignacionDTO buscarAsignacionPorPaqueteID(Integer id) throws NoSuchElementException {
        Asignacion asignacion = asignacionRepo.findByPaqueteId(id)
                .orElseThrow(() -> new NoSuchElementException("Asignacion no encontrada por paquete id " + id));

        return toDTO(asignacion);
    }

    public AsignacionDTO buscarAsignacionPorID(Integer id) throws NoSuchElementException {
        Asignacion asignacion = asignacionRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Asignacion no encontrada" + id));

        return toDTO(asignacion);
    }

    public PaqueteDTO buscarPaquetePorID(Integer id) throws NoSuchElementException {
        Paquete paquete = paqueteRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Paquete no encontrado" + id));

        return toDTO(paquete);
    }

    @Override
    public AsignacionDTO gestionarDonacion(Integer depositoID, String donacionID, String productoID, Integer cantidadDonada)
            throws NoSuchElementException {

        if (cantidadDonada == null || cantidadDonada <= 0) {
            throw new IllegalArgumentException("Cantidad inválida");
        }

        Deposito deposito = depositoRepo.findById(depositoID)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado: " + depositoID));

        if (!deposito.tieneLugar(cantidadDonada)) {
            throw new IllegalArgumentException("El depósito asignado no tiene lugar suficiente");
        }

        // --- REGISTRO DE MÉTRICA 2: Incrementa la cantidad de items/unidades recibidas ---
        this.cantidadDonadaTotalCounter.increment(cantidadDonada);

        DonacionMensajeDTO payload = new DonacionMensajeDTO(
                donacionID,
                depositoID,
                cantidadDonada,
                deposito.tipoAlgoritmo,
                productoID
        );

        publicarMensajeEnCola(payload);

        return null;
    }

    private void publicarMensajeEnCola(DonacionMensajeDTO donacionDTO) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPayload = objectMapper.writeValueAsString(donacionDTO);

            ConnectionFactory factory = new ConnectionFactory();

            // Poner los datos directamente como String:
            factory.setHost("jackal.rmq.cloudamqp.com");
            factory.setUsername("vnfuxbdr");
            factory.setPassword("Rb_2FdMdZG2INVFVrh4yDIyiPbfU859A");
            factory.setVirtualHost("vnfuxbdr");

            String queueName = "cola_donaciones";

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.queueDeclare(queueName, false, false, false, null);
                channel.basicPublish("", queueName, null, jsonPayload.getBytes(StandardCharsets.UTF_8));
                System.out.println("📦 Donación enviada a la cola: " + jsonPayload);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al publicar mensaje en RabbitMQ", e);
        }
    }



    public void guardarEnStock(Integer depositoID, String donacionID, String productoID, Integer cantidadDonada){
        Deposito deposito = depositoRepo.findById(depositoID)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado: " + depositoID));

        if (!deposito.tieneLugar(cantidadDonada)) {
            throw new IllegalArgumentException("El deposito asignado no tiene lugar suficiente");
        }

        Paquete paquete = new Paquete(
                donacionID,
                productoID,
                cantidadDonada
        );

        deposito.agregarPaquete(paquete);
        depositoRepo.save(deposito);

        // --- REGISTRO DE MÉTRICA 1: Nuevo paquete creado en stock ---
        this.paquetesCreadosCounter.increment();
    }

//    public AsignacionDTO gestiowerwernarDonacion(Integer depositoID, String donacionID, String productoID, Integer cantidadDonada)
//            throws NoSuchElementException {
//
//        if (cantidadDonada == null || cantidadDonada <= 0) {
//            throw new IllegalArgumentException("Cantidad inválida");
//        }
//
//        Deposito deposito = depositoRepo.findById(depositoID)
//                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado: " + depositoID));
//
//        if (!deposito.tieneLugar(cantidadDonada)) {
//            throw new IllegalArgumentException("El deposito asignado no tiene lugar suficiente");
//        }
//
//        List<NecesidadMaterialDTO> necesidadesDeProducto =
//                donadoresYEntidadesClient.obtenerNecesidadesInsatisfechasDe(productoID);
//
//        if (necesidadesDeProducto.isEmpty()) {
//
//            Paquete paquete = new Paquete(
//                    donacionID,
//                    productoID,
//                    cantidadDonada
//            );
//
//            deposito.agregarPaquete(paquete);
//            depositoRepo.save(deposito);
//
//            // --- REGISTRO DE MÉTRICA 1 ---
//            this.paquetesCreadosCounter.increment();
//
//            return null;
//        }
//
//        List<NecesidadMaterialDTO> necesidadesAplicables =
//                necesidadesDeProducto.stream()
//                        .filter(n ->
//                                this.necesidadService.esNecesidadAplicable(
//                                        n,
//                                        cantidadDonada
//                                )
//                        )
//                        .toList();
//
//        if (necesidadesAplicables.isEmpty()) {
//            throw new NoSuchElementException(
//                    "No hay necesidades aplicables"
//            );
//        }
//
//        List<NecesidadLogistica> necesidadesLogistica =
//                necesidadesAplicables.stream()
//                        .map(this::toDomain)
//                        .toList();
//
//        NecesidadLogistica elegida =
//                ejecutarMatchmaking(
//                    deposito,
//                    cantidadDonada,
//                    necesidadesLogistica
//                );
//
//        int cantidadNecesitada = elegida.getCantidadFaltante();
//        int cantidadAAsignar = cuantoAsignar(cantidadNecesitada, cantidadDonada);
//
//        Paquete paqueAsignado = new Paquete(
//                donacionID,
//                productoID,
//                cantidadAAsignar
//        );
//
//        Paquete paqueAsignadoyGuardado = paqueteRepo.save(paqueAsignado);
//
//        // --- REGISTRO DE MÉTRICA 1 ---
//        this.paquetesCreadosCounter.increment();
//
//        return AsignarPaquete(
//                paqueAsignado,
//                deposito,
//                elegida.getId(),
//                cantidadDonada,
//                cantidadAAsignar,
//                donacionID,
//                productoID
//                );
//    }

    private OpcionStock buscarMejorOpcionStock(String productoID, int cantidadSolicitada) {
        List<Deposito> depositos = depositoRepo.findAll();
        List<OpcionStock> candidatos = new ArrayList<>();

        for (Deposito deposito : depositos) {
            for (Paquete paquete : deposito.getStockActual()) {
                if (productoID.equals(paquete.getProductoID())) {
                    candidatos.add(new OpcionStock(deposito, paquete));
                }
            }
        }

        if (candidatos.isEmpty()) {
            throw new NoSuchElementException("No hay stock disponible para el producto: " + productoID);
        }

        return candidatos.stream()
                .min(Comparator.comparingInt(opcion ->
                        Math.abs(opcion.paquete().getCantidad() - cantidadSolicitada)))
                .orElseThrow();
    }

    AsignacionDTO AsignarPaquete(
            Paquete paquete,
            Deposito deposito,
            String necesidadID,
            int cantidadTotal,
            int cantidadAAsignar,
            String donacionID,
            String productoID
    ){
        Paquete paqueteGuardado = (paquete.getId() == null) ? paqueteRepo.save(paquete) : paquete;

        Asignacion asignacion = new Asignacion(
                paqueteGuardado.getId(),
                necesidadID,
                LocalDateTime.now(),
                EstadoAsignacionEnum.ASIGNADA,
                AsgnacionRealizadaPor.ALGORITMO_MATCHMAKING
        );

        Asignacion asignacionConId = asignacionRepo.save(asignacion);

        // --- REGISTRO DE MÉTRICAS 3 Y 4 ---
        this.asignacionesMatchmakingCounter.increment();
        this.tamanioAsignacionSummary.record(cantidadAAsignar);

        int cantidadSobrante = cantidadTotal - cantidadAAsignar;
        if(cantidadSobrante > 0){
            Paquete paqueStock = new Paquete(
                    donacionID,
                    productoID,
                    cantidadSobrante
            );

            deposito.agregarPaquete(paqueStock);
            depositoRepo.save(deposito);

            // --- REGISTRO DE MÉTRICA 1 ---
            this.paquetesCreadosCounter.increment();
        }
        return toDTO(asignacionConId);
    }

    @Transactional
    public int asignarProductoAEntidad(NecesidadMaterialDTO necesidad, AsgnacionRealizadaPor tipoAsignacion) {
        String productoID = necesidad.productoSolicitadoID();
        int cantidadNecesitada = necesidad.cantidadObjetivo() - necesidad.cantidadRecibida();

        OpcionStock mejorOpcion = buscarMejorOpcionStock(productoID, cantidadNecesitada);

        Deposito depositoElegido = mejorOpcion.deposito();
        Paquete paqueteElegido = mejorOpcion.paquete();

        int cantidadAAsignar = cuantoAsignar(cantidadNecesitada, paqueteElegido.getCantidad());

        Asignacion asignacion = new Asignacion(
                paqueteElegido.getId(),
                necesidad.id(),
                LocalDateTime.now(),
                EstadoAsignacionEnum.ASIGNADA,
                tipoAsignacion
        );
        asignacionRepo.save(asignacion);

        // --- REGISTRO DE MÉTRICAS 3 Y 4 ---
        this.asignacionesSolicitudExternaCounter.increment();
        this.tamanioAsignacionSummary.record(cantidadAAsignar);

        if (paqueteElegido.getCantidad() == cantidadAAsignar) {
            depositoElegido.removerPaquete(paqueteElegido);
            paqueteRepo.delete(paqueteElegido);
        } else {
            paqueteElegido.restarCantidad(cantidadAAsignar);
        }

        depositoElegido.setCapacidadRestante(depositoElegido.getCapacidadRestante() + cantidadAAsignar);
        depositoRepo.save(depositoElegido);

        return cantidadAAsignar;
    }

    public int cuantoAsignar(int cantidadNecesitada, int cantidadDonada) {
        return Math.min(cantidadNecesitada, cantidadDonada);
    }

    public DepositoDTO cambiarAlgoritmo(
            Integer depositoId,
            TipoAlgoritmoEnum algoritmo
    ) {
        Deposito deposito = depositoRepo
                .findById(depositoId)
                .orElseThrow(() ->
                        new NoSuchElementException("Deposito no encontrado"));

        deposito.setTipoAlgoritmo(algoritmo);
        depositoRepo.save(deposito);

        return toDTO(deposito);
    }

    @Override
    public void setAlgoritmoMM(Integer depositoID, TipoAlgoritmoEnum tipoAlgoritmo) {
        Deposito deposito  = depositoRepo.findById(depositoID)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado: " + depositoID));

        deposito.setTipoAlgoritmo(tipoAlgoritmo);
        depositoRepo.save(deposito);
    }

    private NecesidadLogistica ejecutarMatchmaking(
            Deposito deposito,
            int cantidadDonada,
            List<NecesidadLogistica> necesidadesLogistica) {

        if (cantidadDonada < 0) {
            throw new IllegalArgumentException("no dona nada y hasta roba");
        }

        if (necesidadesLogistica == null || necesidadesLogistica.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades");
        }

        AlgoritmoAsignacion algoritmo = AlgoritmoFactory.crear(deposito.tipoAlgoritmo);

        NecesidadLogistica elegida = algoritmo.elegir(
                necesidadesLogistica,
                cantidadDonada
        );

        if (elegida == null) {
            throw new NoSuchElementException("No se pudo asignar necesidad");
        }
        return elegida;
    }

    @Override
    public void reportarEntrega(PaqueteDTO paqueteDTO) {

        if (paqueteDTO == null) {
            throw new IllegalArgumentException("Paquete inválido");
        }

        Asignacion asignacion =
                asignacionRepo.findByPaqueteId(paqueteDTO.id())
                        .orElseThrow(() ->
                                new NoSuchElementException("Asignacion no encontrada")
                        );

        if(asignacion.getEstado() == EstadoAsignacionEnum.COMPLETADA) {
            return;
        }

        donacionesClient.cambiarEstadoDeDonacion(
                paqueteDTO.donacionID(),
                EstadoDonacionEnum.ACEPTADA
        );

        donadoresYEntidadesClient.satisfacerNecesidad(
                asignacion.getNecesidadId(),
                paqueteDTO.cantidad()
        );

        asignacion.completada();
        asignacionRepo.save(asignacion);
    }

    // toDTO
    private DepositoDTO toDTO(Deposito deposito) {
        return new DepositoDTO(
                deposito.getId(),
                deposito.getTipoAlgoritmo(),
                deposito.getNombre(),
                deposito.getDireccion(),
                deposito.getCapacidadMaxima(),
                deposito.getStockActual().stream().map(this::toDTO).toList()
        );
    }

    private PaqueteDTO toDTO(Paquete paquete) {
        return new PaqueteDTO(
                paquete.getId(),
                paquete.getDonacionID(),
                paquete.getProductoID(),
                paquete.getCantidad()
        );
    }

    private AsignacionDTO toDTO(Asignacion asignacion) {
        return new AsignacionDTO(
                asignacion.getId(),
                asignacion.getPaqueteId(),
                asignacion.getNecesidadId(),
                asignacion.getFecha(),
                asignacion.getEstado()
        );
    }

    // ToDomain
    private Deposito toDomain(DepositoDTO dto) {
        return new Deposito(
                dto.algoritmo(),
                dto.nombre(),
                dto.direccion(),
                dto.capacidadMaxima()
        );
    }

    private Paquete toDomain(PaqueteDTO dto) {
        return new Paquete(
                dto.donacionID(),
                dto.producto(),
                dto.cantidad()
        );
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
}
