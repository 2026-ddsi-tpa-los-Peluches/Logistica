package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionMensajeDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.componentes.DonacionesClient;
import ar.edu.utn.dds.k3003.componentes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.controllers.requests.AsignacionRequest;
import ar.edu.utn.dds.k3003.publisher.AsignacionPublisher;
import ar.edu.utn.dds.k3003.publisher.DepositosPublisher;
import ar.edu.utn.dds.k3003.publisher.LogisticaPublisher;
import ar.edu.utn.dds.k3003.repositories.asignaciones.AsignacionesRepository;
import ar.edu.utn.dds.k3003.repositories.depositos.DepositosRepository;
import ar.edu.utn.dds.k3003.repositories.paquetes.PaquetesRepository;
import ar.edu.utn.dds.k3003.worker.model.AlgoritmoAsignacion;
import ar.edu.utn.dds.k3003.worker.model.AlgoritmoFactory;
import ar.edu.utn.dds.k3003.worker.model.NecesidadLogistica;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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

    private final ApplicationEventPublisher publicadorSpring;
    private final RabbitTemplate rabbitTemplate;


    // REPOS
    private final DepositosRepository depositoRepo;
    private final AsignacionesRepository asignacionRepo;
    private final PaquetesRepository paqueteRepo;


    // PUBLISHERS
    private final LogisticaPublisher logisticaPublisher;
    private final DepositosPublisher depositosPublisher;
    private final AsignacionPublisher asignacionPublisher;

    @Autowired
    public Fachada(
            MeterRegistry meterRegistry,
            DonadoresYEntidadesClient donadoresYEntidadesClient,
            DonacionesClient donacionesClient,
            DepositosRepository depositoRepo,
            AsignacionesRepository asignacionRepo,
            PaquetesRepository paqueteRepo,
            ApplicationEventPublisher publicadorSpring,
            RabbitTemplate rabbitTemplate,
            LogisticaPublisher logisticaPublisher,
            DepositosPublisher depositosPublisher,
             AsignacionPublisher asignacionPublisher

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


        this.publicadorSpring = publicadorSpring;
        this.rabbitTemplate = rabbitTemplate;

        this.logisticaPublisher = logisticaPublisher;
        this.depositosPublisher = depositosPublisher;
        this.asignacionPublisher = asignacionPublisher;
    }

    @Override
    public DepositoDTO agregarDeposito(DepositoDTO depositoDTO) {
        if (depositoDTO == null || depositoDTO.id() != null) {
            log.warn("Alta de depósito rechazada: depósito inválido");
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

        depositosPublisher.notificarDepositoCreado(depositoConId);

        log.info("Depósito creado: id={}, nombre={}, capacidadMaxima={}",
                depositoConId.getId(), depositoDTO.nombre(), depositoDTO.capacidadMaxima());

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
                .orElseThrow(() -> {
                    log.warn("No se pudo eliminar el depósito {}: no encontrado", id);
                    return new NoSuchElementException("Depósito no encontrado: " + id);
                });

        depositoRepo.delete(deposito);

        depositosPublisher.notificarDepositoEliminado(id);

        log.info("Depósito eliminado: id={}", id);
    }
//
//    @Override
//    public AsignacionDTO buscarAsignacionPorPaqueteID(Integer id) throws NoSuchElementException {
//        Asignacion asignacion = asignacionRepo.findByPaqueteId(id)
//                .orElseThrow(() -> new NoSuchElementException("Asignacion no encontrada por paquete id " + id));
//
//        return toDTO(asignacion);
//    }

    public AsignacionDTO buscarAsignacionPorID(Integer id) throws NoSuchElementException {
        log.info("Consulta de Asignacion realizado por ID con exito: asignacionID={}",id);
        Asignacion asignacion = asignacionRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Asignacion no encontrada" + id));

        return toDTO(asignacion);
    }

    public PaqueteDTO buscarPaquetePorID(Integer id) throws NoSuchElementException {
        log.info("Consulta de Paquete realizada por ID con exito: donacionID={}",id);
        Paquete paquete = paqueteRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Paquete no encontrado" + id));

        return toDTO(paquete);
    }

    @Override
    public AsignacionDTO gestionarDonacion(Integer depositoID, String donacionID, String productoID, Integer cantidadDonada)
            throws NoSuchElementException {

        if (cantidadDonada == null || cantidadDonada <= 0) {
            log.warn("Donación {} rechazada: cantidad inválida ({})", donacionID, cantidadDonada);
            throw new IllegalArgumentException("Cantidad inválida");
        }

        Deposito deposito = depositoRepo.findById(depositoID)
                .orElseThrow(() -> {
                    log.warn("Donación {} rechazada: depósito {} no encontrado", donacionID, depositoID);
                    return new NoSuchElementException("Depósito no encontrado: " + depositoID);
                });

        if (!deposito.tieneLugar(cantidadDonada)) {
            log.warn("Donación {} rechazada: el depósito {} no tiene lugar para {} unidades",
                    donacionID, depositoID, cantidadDonada);
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

        log.info("Donación recibida y publicada en la cola: donacionID={}, depositoID={}, productoID={}, cantidad={}",
                donacionID, depositoID, productoID, cantidadDonada);

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
                log.info("Mensaje publicado en la cola {}: {}", queueName, jsonPayload);
            }
        } catch (Exception e) {
            log.error("Error al publicar la donación en RabbitMQ", e);
            throw new RuntimeException("Error al publicar mensaje en RabbitMQ", e);
        }
    }



    public void guardarEnStock(Integer depositoID, String donacionID, String productoID, Integer cantidadDonada){
        Deposito deposito = depositoRepo.findById(depositoID)
                .orElseThrow(() -> {
                    log.warn("No se pudo guardar en stock la donación {}: depósito {} no encontrado", donacionID, depositoID);
                    return new NoSuchElementException("Depósito no encontrado: " + depositoID);
                });

        if (!deposito.tieneLugar(cantidadDonada)) {
            log.warn("No se pudo guardar en stock la donación {}: el depósito {} no tiene lugar para {} unidades",
                    donacionID, depositoID, cantidadDonada);
            throw new IllegalArgumentException("El deposito asignado no tiene lugar suficiente");
        }

        Paquete paquete = new Paquete(
                donacionID,
                productoID,
                cantidadDonada,
                depositoID
        );

        deposito.agregarPaquete(paquete);
        depositoRepo.save(deposito);

        DonacionDTO donacionDTO = donacionesClient.obtenerDonacionPorId(donacionID);
        depositosPublisher.notificarDonacionIngresada(depositoID, donacionID, donacionDTO.donadorID(), cantidadDonada);

        depositosPublisher.notificarPaqueteAgregado(depositoID, paquete);

        // aviso al repartidor de chocolate
        logisticaPublisher.notificarStockProductoModificado(paquete, deposito, cantidadDonada);
        // ------------------------------------------

        // --- REGISTRO DE MÉTRICA 1: Nuevo paquete creado en stock ---
        this.paquetesCreadosCounter.increment();

        log.info("Paquete guardado en stock: donacionID={}, productoID={}, cantidad={}, depositoID={}",
                donacionID, productoID, cantidadDonada, depositoID);
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
        List<OpcionStock> candidatos = getOpcionStocks(productoID, depositos);

        // Primero intentamos encontrar paquetes que alcancen la cantidad solicitada
        return candidatos.stream()
                .filter(opcion -> opcion.paquete().getCantidad() >= cantidadSolicitada)
                .min(Comparator.comparingInt(opcion ->
                        opcion.paquete().getCantidad()
                ))
                // Si ninguno alcanza, elegimos el más cercano igualmente
                .orElseGet(() -> candidatos.stream()
                        .min(Comparator.comparingInt(opcion ->
                                Math.abs(opcion.paquete().getCantidad() - cantidadSolicitada)
                        ))
                        .orElseThrow()
                );
    }

    private List<OpcionStock> getOpcionStocks(String productoID, List<Deposito> depositos) {
        List<OpcionStock> candidatos = new ArrayList<>();
        for (Deposito deposito : depositos) {
            for (Paquete paquete : deposito.getStockActual()) {
                if (productoID.equals(paquete.getProductoID())) {
                    candidatos.add(new OpcionStock(deposito, paquete));
                }
            }
        }

        if (candidatos.isEmpty()) {
            log.warn("No hay stock disponible para el producto {}", productoID);
            throw new NoSuchElementException(
                    "No hay stock disponible para el producto: " + productoID
            );
        }
        return candidatos;
    }

    AsignacionDTO AsignarPaquete(
            Paquete paquete,
            Deposito deposito,
            String necesidadID,
            int cantidadAAsignar,
            String donacionID,
            String productoID
    ){

        Asignacion asignacion = new Asignacion(
                paquete.getId(),
                necesidadID,
                LocalDateTime.now(),
                EstadoAsignacionEnum.ASIGNADA,
                AsgnacionRealizadaPor.ALGORITMO_MATCHMAKING,
                cantidadAAsignar
        );

        Asignacion asignacionConId = asignacionRepo.save(asignacion);

        DonacionDTO donacionDTO = donacionesClient.obtenerDonacionPorId(donacionID);

        // aviso a rabbit que llego la donacion a logistica para documento de donacion
        depositosPublisher.notificarDonacionIngresada(deposito.getId(), donacionID, donacionDTO.donadorID(), cantidadAAsignar );

        // aviso a rabbit que cambio el estado de la asignacion para documento de deposito
        depositosPublisher.notificarAsignacionEstado(deposito.getId(), EstadoAsignacionEnum.ASIGNADA);

        NecesidadMaterialDTO necesidadMaterialDTO = donadoresYEntidadesClient.obtenerNecesidadPorId(necesidadID);


        // aviso conejo asignacion creada para documento de asignacion
        asignacionPublisher.notificarAsignacionCreada(asignacionConId, deposito, donacionID, necesidadMaterialDTO, AsgnacionRealizadaPor.ALGORITMO_MATCHMAKING);

        // --- REGISTRO DE MÉTRICAS 3 Y 4 ---
        this.asignacionesMatchmakingCounter.increment();
        this.tamanioAsignacionSummary.record(cantidadAAsignar);


        log.info("Asignación creada (matchmaking): id={}, paqueteID={}, necesidadID={}, cantidad={}",
                asignacionConId.getId(), asignacionConId.getPaqueteId(), necesidadID, cantidadAAsignar);

        return toDTO(asignacionConId);
    }

    @Transactional
    public int asignarProductoAEntidad(NecesidadMaterialDTO necesidad) {
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
                AsgnacionRealizadaPor.SOLICITUD_EXTERNA,
                cantidadAAsignar
        );

        Asignacion asignacionConId = asignacionRepo.save(asignacion);

        depositosPublisher.notificarAsignacionEstado(depositoElegido.getId(), EstadoAsignacionEnum.ASIGNADA);

        logisticaPublisher.notificarNecesidadAsignacionPendiente(asignacionConId, depositoElegido, cantidadAAsignar);

        EntidadBeneficaDTO entidad = donadoresYEntidadesClient.obtenerEntidadPorId(Integer.valueOf(necesidad.entidadID()));

        depositosPublisher.notificarEntidadAtendida(depositoElegido.getId(), necesidad, cantidadAAsignar, entidad );


        // aviso asignacion por solicitud externa
        asignacionPublisher.notificarAsignacionCreada(
                asignacion,
                depositoElegido,
                paqueteElegido.getDonacionID(),
                necesidad,
                AsgnacionRealizadaPor.SOLICITUD_EXTERNA
        );


        // --- REGISTRO DE MÉTRICAS 3 Y 4 ---
        this.asignacionesSolicitudExternaCounter.increment();
        this.tamanioAsignacionSummary.record(cantidadAAsignar);




        paqueteElegido.restarCantidad(cantidadAAsignar);



        depositoElegido.setCapacidadRestante(depositoElegido.getCapacidadRestante() + cantidadAAsignar);
        depositoRepo.save(depositoElegido);

        depositosPublisher.notificarPaqueteStockActualizado(depositoElegido.getId(), paqueteElegido, cantidadAAsignar);

        logisticaPublisher.notificarStockProductoModificado(paqueteElegido, depositoElegido, cantidadAAsignar);


        log.info("Producto asignado a entidad: necesidadID={}, productoID={}, cantidad={}, depositoID={}, origen={}",
                necesidad.id(), productoID, cantidadAAsignar, depositoElegido.getId(),  AsgnacionRealizadaPor.SOLICITUD_EXTERNA);

        return cantidadAAsignar;
    }

    @Transactional
    public AsignacionDTO asignarDesdeDonacion(AsignacionRequest request) {
        Deposito deposito = depositoRepo.findById(request.depositoID())
                .orElseThrow(() -> {
                    log.warn("No se pudo asignar desde la donación {}: depósito {} no encontrado",
                            request.donacionID(), request.depositoID());
                    return new NoSuchElementException("Depósito no encontrado: " + request.depositoID());
                });

        int cantidadAAsignar = cuantoAsignar(request.cantidadNecesitada(), request.cantidad());



        Paquete paquete = new Paquete(
                request.donacionID(),
                request.productoID(),
                request.cantidad() - cantidadAAsignar,
                deposito.getId()
        );

        return AsignarPaquete(paquete, deposito, request.necesidadID(), request.cantidad(), cantidadAAsignar, request.donacionID(), request.productoID()
        );
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
                .orElseThrow(() -> {
                    log.warn("No se pudo cambiar el algoritmo: depósito {} no encontrado", depositoId);
                    return new NoSuchElementException("Deposito no encontrado");
                });

        deposito.setTipoAlgoritmo(algoritmo);
        depositoRepo.save(deposito);

        depositosPublisher.notificarAlgoritmoCambiado(depositoId, algoritmo);

        log.info("Algoritmo de depósito cambiado: depositoID={}, algoritmo={}", depositoId, algoritmo);

        return toDTO(deposito);
    }

    @Override
    public void setAlgoritmoMM(Integer depositoID, TipoAlgoritmoEnum tipoAlgoritmo) {
        Deposito deposito  = depositoRepo.findById(depositoID)
                .orElseThrow(() -> {
                    log.warn("No se pudo cambiar el algoritmo: depósito {} no encontrado", depositoID);
                    return new NoSuchElementException("Depósito no encontrado: " + depositoID);
                });

        deposito.setTipoAlgoritmo(tipoAlgoritmo);
        depositoRepo.save(deposito);
        // --- PUBLICAR EVENTO A RABBITMQ PARA EL SERVICIO DE CONSULTAS ---
//        DepositoActualizadoEvent evento = new DepositoActualizadoEvent(
//                String.valueOf(deposito.getId()),
//                deposito.getNombre(),
//                deposito.getCapacidadMaxima(),
//                deposito.getTipoAlgoritmo() != null ? deposito.getTipoAlgoritmo().name() : "DEFAULT",
//                Instant.now()
//        );
//        rabbitTemplate.convertAndSend("donatrack.eventos", "deposito", evento);

        log.info("Algoritmo de depósito cambiado: depositoID={}, algoritmo={}", depositoID, tipoAlgoritmo);
    }

    private NecesidadLogistica ejecutarMatchmaking(
            Deposito deposito,
            int cantidadDonada,
            List<NecesidadLogistica> necesidadesLogistica) {

        if (cantidadDonada < 0) {
            log.warn("Matchmaking rechazado: cantidad donada negativa ({})", cantidadDonada);
            throw new IllegalArgumentException("no dona nada y hasta roba");
        }

        if (necesidadesLogistica == null || necesidadesLogistica.isEmpty()) {
            log.warn("Matchmaking sin necesidades disponibles");
            throw new NoSuchElementException("No hay necesidades");
        }

        AlgoritmoAsignacion algoritmo = AlgoritmoFactory.crear(deposito.tipoAlgoritmo);

        NecesidadLogistica elegida = algoritmo.elegir(
                necesidadesLogistica,
                cantidadDonada
        );

        if (elegida == null) {
            log.warn("Matchmaking no pudo elegir una necesidad (cantidadDonada={})", cantidadDonada);
            throw new NoSuchElementException("No se pudo asignar necesidad");
        }
        return elegida;
    }

    @Override
    public void reportarEntrega(Integer asignacionId) {

        if (asignacionId == null) {
            log.warn("Reporte de entrega rechazado: asignación inválida");
            throw new IllegalArgumentException("Asignacion inválida");
        }


        Asignacion asignacion =
                asignacionRepo.findById(asignacionId)
                        .orElseThrow(() -> {
                            log.warn("Reporte de entrega rechazado: asignación {} no encontrada", asignacionId);
                            return new NoSuchElementException("Asignacion no encontrada");
                        });

        if(asignacion.getEstado() == EstadoAsignacionEnum.COMPLETADA) {
            log.info("Entrega de la asignación {} ya estaba reportada", asignacionId);
            return;
        }

        Paquete paquete = paqueteRepo.findById(asignacion.getPaqueteId())
                .orElseThrow(() -> {
                    log.warn("Reporte de entrega rechazado: paquete {} de la asignación {} no encontrado",
                            asignacion.getPaqueteId(), asignacionId);
                    return new NoSuchElementException("Paquete no encontrado");
                });

        if(paquete.getCantidad() == 0)
        {
            donacionesClient.cambiarEstadoDeDonacion(
                    paquete.getDonacionID(),
                    EstadoDonacionEnum.ACEPTADA
            );
        }


//        donadoresYEntidadesClient.satisfacerNecesidad(
//                asignacion.getNecesidadId(),
//                asignacion.getCantidad()
//        );
//el motivo por el cual se comenta este llamado es porque nuestra organizacion tiene el registro de que quedo asignado
        // a que necesidad , entonces , como manejamos el microservicio de donadroesyentidades , y tambien el de logistica
        //tenemos una columna "cantidad_Recibida" la cual almacena el valor de la cantidad asignada. y cuando realmente
        //se entrega el pedido ahi recien se actualiza la donacion y la asignacion.

        asignacion.completada();
        asignacionRepo.save(asignacion);

        depositosPublisher.notificarDonacionEntregada(paquete.getDepositoID(), paquete.getDonacionID());

        // le aviso al donejo para actualizar deposito
        depositosPublisher.notificarAsignacionEstado(paquete.getDepositoID(), EstadoAsignacionEnum.COMPLETADA);

        // le aviso al donejo para actualizar asignacion
        asignacionPublisher.notificarAsignacionEstado(asignacionId, EstadoAsignacionEnum.COMPLETADA);


        // llamo a rabbiol
        logisticaPublisher.notificarNecesidadAsignacionHecha(asignacion);


        // se junta la banda
        DonacionDTO donacionDTO = donacionesClient.obtenerDonacionPorId(paquete.getDonacionID());
        NecesidadMaterialDTO necesidadMaterialDTO = donadoresYEntidadesClient.obtenerNecesidadPorId(asignacion.getNecesidadId());
        EntidadBeneficaDTO entidadBeneficaDTO = donadoresYEntidadesClient.obtenerEntidadPorId(Integer.valueOf(necesidadMaterialDTO.entidadID()));

        // envio conejo chocolat
        logisticaPublisher.notificarDonacionDonadorEntregada(
                asignacion,
                paquete,
                donacionDTO,
                necesidadMaterialDTO,
                entidadBeneficaDTO
        );

        log.info("Entrega reportada: asignacionID={}, donacionID={}", asignacionId, paquete.getDonacionID());
    }

    // toDTO
    private DepositoDTO toDTO(Deposito deposito) {
        // 1. Agrupamos los paquetes por producto y sumamos las cantidades de cada grupo
        List<PaqueteDTO> stockAgrupado = deposito.getStockActual().stream()
                .collect(Collectors.groupingBy(
                        Paquete::getProductoID, // o getProductoId(), según cómo lo tengas en tu entidad Paquete
                        Collectors.summingInt(Paquete::getCantidad)
                ))
                .entrySet().stream()
                .map(entry -> new PaqueteDTO(
                        null,             // id del paquete individual (no aplica al agrupar)
                        null,             // donacionID individual (no aplica al agrupar)
                        entry.getKey(),   // Producto / ProductoID
                        entry.getValue()  // Cantidad total sumada
                ))
                .toList();

        // 2. Retornamos el DepositoDTO con la lista consolidada
        return new DepositoDTO(
                deposito.getId(),
                deposito.getTipoAlgoritmo(),
                deposito.getNombre(),
                deposito.getDireccion(),
                deposito.getCapacidadMaxima(),
                stockAgrupado
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
                asignacion.getEstado(),
                asignacion.getCantidad()
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

    public List<DepositoStockDTO> obtenerTodoElStock() {
        return depositoRepo.findAll().stream()
                .flatMap(deposito -> deposito.getStockActual().stream() // o getStockActual(), según el nombre de la relación en tu entidad Deposito
                        .map(paquete -> new DepositoStockDTO(
                                deposito.getId(),
                                paquete.getId()
                        ))
                )
                .toList();
    }



}