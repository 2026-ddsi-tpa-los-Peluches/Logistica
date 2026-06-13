package ar.edu.utn.dds.k3003;

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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class Fachada implements FachadaLogistica {

    //CLIENTS
    private final DonadoresYEntidadesClient donadoresYEntidadesClient;
    private final DonacionesClient donacionesClient;

    //METRICAS
    private final Counter depositosCreadosCounter;

    //VERIF
    private final NecesidadService necesidadService;


    //REPOS
    private final DepositosRepository depositoRepo;
    private final AsignacionesRepository asignacionRepo;
    private final PaquetesRepository paqueteRepo;

    @Autowired
    public Fachada(
            MeterRegistry meterRegistry,
            NecesidadService necesidadService,
            DonadoresYEntidadesClient donadoresYEntidadesClient,
            DonacionesClient donacionesClient,
            DepositosRepository depositoRepo,
            AsignacionesRepository asignacionRepo,
            PaquetesRepository paqueteRepo
    ) {
        this.depositosCreadosCounter = Counter.builder("deposito.creados")
                .description("Deposito creado")
                .register(meterRegistry);

        this.necesidadService = necesidadService;
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
                depositoDTO.capacidadMaxima(),
                new ArrayList<>()
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
    public AsignacionDTO gestionarDonacion(Integer depositoID, String donacionID, String productoID, Integer cantidad)
            throws NoSuchElementException {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad inválida");
        }

        Deposito deposito = depositoRepo.findById(depositoID)
                        .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado: " + depositoID));

        List<NecesidadMaterialDTO> necesidadesDeProducto =
                donadoresYEntidadesClient.obtenerNecesidadesInsatisfechasDe(productoID);


        if (necesidadesDeProducto.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades para este producto");
        }

        Paquete paquete = new Paquete(
                donacionID,
                productoID,
                cantidad
        );

        List<NecesidadMaterialDTO> necesidadesAplicables =
                necesidadesDeProducto.stream()
                        .filter(n ->
                                                    this.necesidadService.esNecesidadAplicable(
                                                            n,
                                                            paquete.getCantidad()
                                                    )
                        )
                        .toList();

        if(necesidadesAplicables.isEmpty()) {
            throw new NoSuchElementException(
                    "No hay necesidades aplicables"
            );
        }

        Paquete paqueteGuardado = paqueteRepo.save(paquete);

        return ejecutarMatchmaking(
                deposito,
                paqueteGuardado,
                necesidadesAplicables
        );
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

    private AsignacionDTO ejecutarMatchmaking(
            Deposito deposito,
            Paquete paquete,
            List<NecesidadMaterialDTO> necesidades) {


        if (paquete == null) {
            throw new IllegalArgumentException("Paquete nulo");
        }

        if (necesidades == null || necesidades.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades");
        }




        List<NecesidadLogistica> necesidadesLogistica =
                necesidades.stream()
                        .map(this::toDomain)
                        .toList();

        AlgoritmoAsignacion algoritmo =
                AlgoritmoFactory.crear(deposito.tipoAlgoritmo);


        NecesidadLogistica elegida = algoritmo.elegir(
                necesidadesLogistica,
                paquete.getCantidad()
        );

        if(elegida == null) {
            throw new NoSuchElementException(
                    "No se pudo asignar necesidad"
            );
        }

        Asignacion asignacion = new Asignacion(
                paquete.getId(),
                elegida.getId(),
                LocalDateTime.now(),
                EstadoAsignacionEnum.ASIGNADA
        );

        Asignacion asignacionConId =
                asignacionRepo.save(asignacion);

        return toDTO(asignacionConId);
    }

    @Override
    public void reportarEntrega(PaqueteDTO paqueteDTO) {

        if (paqueteDTO == null) {
            throw new IllegalArgumentException("Paquete inválido");
        }

        Asignacion asignacion =
                asignacionRepo.findByPaqueteId(paqueteDTO.id())
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Asignacion no encontrada"
                                )
                        );

        if(asignacion.getEstado() ==
                EstadoAsignacionEnum.COMPLETADA) {
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
                dto.capacidadMaxima(),
                dto.stockActual() == null
                        ? new ArrayList<>()
                        : dto.stockActual().stream().map(this::toDomain).toList()
        );
    }

    private Paquete toDomain(PaqueteDTO dto) {
        return new Paquete(
                dto.donacionID(),
                dto.producto(),
                dto.cantidad()
        );
    }


    private Asignacion toDomain(AsignacionDTO dto) {
        return new Asignacion(
                dto.paqueteID(),
                dto.necesidadID(),
                dto.fecha(),
                dto.estado()
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
