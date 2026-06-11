package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
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
    private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
    private FachadaDonaciones fachadaDonaciones;
    private final Counter DepositosCreadosCounter;
    private final NecesidadService necesidadService;


    @Autowired
    public Fachada(MeterRegistry meterRegistry) {
        this.DepositosCreadosCounter = Counter.builder("deposito.creados")
                .description("Deposito creado")
                .register(meterRegistry);
        this.necesidadService = new NecesidadService();
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
        this.DepositosCreadosCounter.increment();

        Deposito depositoConId = depositoRepo.save(deposito);

        return toDTO(depositoConId);
    }

    @Override
    public DepositoDTO buscarDepositoPorID(Integer id) throws NoSuchElementException {

        val deposito = depositoRepo.findById(id);

        if (deposito.isEmpty()) {
            throw new NoSuchElementException("No existe el depósito");
        }

        return toDTO(deposito.get());
    }

    public List<DepositoDTO> obtenerDepositos(){
        return this.depositoRepo.findAll().stream().map(this::toDTO).toList();
    }

    public DepositoDTO borrarDepositoPorID(Integer id){
        val deposito = depositoRepo.findById(id);;
        this.depositoRepo.delete(deposito.get());
        return toDTO(deposito.get()); // logica cuestionable y codigo mas no funca mepa
    }

    @Override
    public AsignacionDTO buscarAsignacionPorPaqueteID(Integer id) throws NoSuchElementException {

        val asignacion = asignacionRepo.findById(id);

        if (asignacion.isEmpty()) {
            throw new NoSuchElementException("No existe asignación");
        }
        return toDTO(asignacion.get());
    }

    public AsignacionDTO buscarAsignacionPorID(Integer id) throws NoSuchElementException {

        val asignacion = asignacionRepo.findById(id);
        if (asignacion.isEmpty()) {
            throw new NoSuchElementException("No existe asignación");
        }
        return toDTO(asignacion.get());
    }

    public PaqueteDTO buscarPaquetePorID(Integer id) throws NoSuchElementException {

        val paquete = paqueteRepo.findById(id);
        if (paquete.isEmpty()) {
            throw new NoSuchElementException("No existe asignación");
        }
        return toDTO(paquete.get());
    }

    @Override
    public DepositoDTO gestionarDonacion(Integer depositoID, String donacionID, String productoID, Integer cantidad)
            throws NoSuchElementException {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad inválida");
        }

        val depositoOptional = depositoRepo.findById(depositoID);


        if (depositoOptional.isEmpty()) {
            throw new NoSuchElementException("No existe el depósito");
        }
        val deposito = depositoOptional.get();

        List<NecesidadMaterialDTO> necesidadesDeProducto =
                fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe(productoID);


        if (necesidadesDeProducto.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades para este producto");
        }



        Paquete paquete = paqueteRepo.save(new Paquete(donacionID, productoID, cantidad));



        List<NecesidadMaterialDTO> necesidadesAplicables = necesidadesDeProducto.stream().filter(necesidadDeProducto ->
                this.necesidadService.esNecesidadAplicable(necesidadDeProducto, paquete.getCantidad())).toList();

        AsignacionDTO asignacionDTO = ejecutarMatchmaking(deposito.getId(), toDTO(paquete), necesidadesAplicables);

        //fachadaDonadoresYEntidades.satisfacerNecesidad(asignacionDTO.necesidadID(), paquete.cantidad());

        return toDTO(deposito); //raro que retorne deposito
    }

    @Override
    public void setAlgoritmoMM(Integer depositoID, TipoAlgoritmoEnum tipoAlgoritmo) {
        val depositoOptional = depositoRepo.findById(depositoID);
        if (depositoOptional.isEmpty()) {
            throw new NoSuchElementException();
        }
        val deposito = depositoOptional.get();
        deposito.setTipoAlgoritmo(tipoAlgoritmo);
        depositoRepo.save(deposito);
    }

    @Override
    public AsignacionDTO ejecutarMatchmaking(Integer depositoID, PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidades) {
        if (paqueteDTO == null) {
            throw new RuntimeException("Paquete nulo");
        }
        if (necesidades.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades");
        }

        val depositoOptional = depositoRepo.findById(depositoID);
        if (depositoOptional.isEmpty()) {
            throw new NoSuchElementException();
        }
        val deposito = depositoOptional.get();

        Paquete paquete = toDomain(paqueteDTO);

        List<NecesidadLogistica> necesidadesLogistica = necesidades.stream().map(this::toDomain).toList();

        AlgoritmoAsignacion algoritmo = AlgoritmoFactory.crear(deposito.tipoAlgoritmo);
        NecesidadLogistica elegida = algoritmo.elegir(necesidadesLogistica, paquete.getCantidad());


        //if(elegida.getId() == null){
         //   elegida.setId("necesidad1");
       // }




        Asignacion asignacion = new Asignacion(
                paquete.getId(),
                elegida.getId(),
                LocalDateTime.now(),
                EstadoAsginacionEnum.ASIGNADA
        );

        Asignacion asignacionConId = asignacionRepo.save(asignacion);

        return toDTO(asignacionConId);
    }

    @Override
    public void reportarEntrega(PaqueteDTO paqueteDTO) {
        if (paqueteDTO == null) {
            throw new RuntimeException("Paquete nulo");
        }

        fachadaDonaciones.cambiarEstadoDeDonacion(
                paqueteDTO.donacionID(),
                EstadoDonacionEnum.ACEPTADA
        );

        val asignacionOptional = asignacionRepo.findByPaqueteId(paqueteDTO.id()); // no se como hacerlo realmenbte

        if (asignacionOptional.isEmpty()) {
            throw new NoSuchElementException("No existe asignación para ese paquete");
        }

        val asignacion = asignacionOptional.get();

        fachadaDonadoresYEntidades.satisfacerNecesidad(
                asignacion.getNecesidadId(),
                paqueteDTO.cantidad()
        );



        asignacion.completada();
    }

    @Override
    public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {
        this.fachadaDonadoresYEntidades = fachadaDonadoresYEntidades;
    }

    @Override
    public void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones) {
        this.fachadaDonaciones = fachadaDonaciones;
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
