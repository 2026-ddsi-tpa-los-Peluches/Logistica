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
    private final DonadoresYEntidadesClient donadoresYEntidadesClient;
    private final DonacionesClient donacionesClient;
    private final Counter depositosCreadosCounter;
    private final NecesidadService necesidadService;


    @Autowired
    public Fachada(MeterRegistry meterRegistry,
                   NecesidadService necesidadService,
                   DonadoresYEntidadesClient donadoresYEntidadesClient,
                   DonacionesClient donacionesClient
    ) {

        this.depositosCreadosCounter = Counter.builder("deposito.creados")
                .description("Deposito creado")
                .register(meterRegistry);
        this.necesidadService = necesidadService;

        this.donadoresYEntidadesClient = donadoresYEntidadesClient;
        this.donacionesClient = donacionesClient;
    }

    @Autowired
    DepositosRepository depositoRepo;

    @Autowired
    AsignacionesRepository asignacionRepo;

    @Autowired
    PaquetesRepository paqueteRepo;

    @Override
    public DepositoDTO agregarDeposito(DepositoDTO depositoDTO) {
        if (depositoDTO == null || depositoDTO.id() != null) {
            throw new RuntimeException("Deposito inválido");
        }

        Deposito deposito = new Deposito(
                null,
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
    public DepositoDTO gestionarDonacion(Integer depositoID, String donacionID, String productoID, Integer cantidad)
            throws NoSuchElementException {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad inválida");
        }

        Deposito deposito = depositoRepo.findById(depositoID).
                orElseThrow(() -> new NoSuchElementException("Depósito no encontrado: " + depositoID));

        List<NecesidadMaterialDTO> necesidadesDeProducto =
                donadoresYEntidadesClient.obtenerNecesidadesInsatisfechasDe(productoID);


        if (necesidadesDeProducto.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades para este producto");
        }



        Paquete paquete = paqueteRepo.save(new Paquete(donacionID, productoID, cantidad));



        List<NecesidadMaterialDTO> necesidadesAplicables = necesidadesDeProducto.stream().filter(necesidadDeProducto ->
                this.necesidadService.esNecesidadAplicable(necesidadDeProducto, paquete.getCantidad())).toList();

        AsignacionDTO asignacionDTO = ejecutarMatchmaking(deposito.getId(), toDTO(paquete), necesidadesAplicables);

        //fachadaDonadoresYEntidades.satisfacerNecesidad(asignacionDTO.necesidadID(), paquete.cantidad());

        return toDTO(deposito);
    }

    @Override
    public void setAlgoritmoMM(Integer depositoID, TipoAlgoritmoEnum tipoAlgoritmo) {
        Deposito deposito  = depositoRepo.findById(depositoID)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado: " + depositoID));

        deposito.setTipoAlgoritmo(tipoAlgoritmo);
        depositoRepo.save(deposito);
    }

    @Override
    public AsignacionDTO ejecutarMatchmaking(Integer depositoID, PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidades) {
        if (paqueteDTO == null) {
            throw new IllegalArgumentException("Paquete nulo");
        }
        if (necesidades == null || necesidades.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades");
        }

        Deposito deposito = depositoRepo.findById(depositoID)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado: " + depositoID));


        Paquete paquete = paqueteRepo.findById(paqueteDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Paquete no encontrado: " + paqueteDTO.id()));

        List<NecesidadLogistica> necesidadesLogistica = necesidades.stream().map(this::toDomain).toList();

        AlgoritmoAsignacion algoritmo = AlgoritmoFactory.crear(deposito.tipoAlgoritmo);

        NecesidadLogistica elegida = algoritmo.elegir(necesidadesLogistica, paquete.getCantidad());




        Asignacion asignacion = new Asignacion(
                paquete.getId(),
                elegida.getId(),
                LocalDateTime.now(),
                EstadoAsignacionEnum.ASIGNADA
        );

        Asignacion asignacionConId = asignacionRepo.save(asignacion);

        return toDTO(asignacionConId);
    }

    @Override
    public void reportarEntrega(PaqueteDTO paqueteDTO) {
        if (paqueteDTO == null) {
            throw new RuntimeException("Paquete vacio");
        }

        donacionesClient.cambiarEstadoDeDonacion(
                paqueteDTO.donacionID(),
                EstadoDonacionEnum.ACEPTADA
        );

        Asignacion asignacion = asignacionRepo.findByPaqueteId(paqueteDTO.id())
                .orElseThrow(() -> new NoSuchElementException("Asignacion no encontrada por paquete id " + paqueteDTO.id()));

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
                  dto.cantidadObjetivo()
          );
      }
}
