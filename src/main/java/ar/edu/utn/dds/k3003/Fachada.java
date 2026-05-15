package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.repositories.asignaciones.AsignacionesRepository;
import ar.edu.utn.dds.k3003.repositories.asignaciones.InMemoryAsignacionesRepository;
import ar.edu.utn.dds.k3003.repositories.depositos.DepositosRepository;
import ar.edu.utn.dds.k3003.repositories.depositos.InMemoryDepositosRepository;
import ar.edu.utn.dds.k3003.repositories.paquetes.InMemoryPaquetesRepository;
import ar.edu.utn.dds.k3003.repositories.paquetes.PaquetesRepository;
import lombok.val;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class Fachada implements FachadaLogistica {
    private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
    private FachadaDonaciones fachadaDonaciones;
    private final DepositosRepository depositoRepository;
    private final AsignacionesRepository asignacionRepository;
    private final PaquetesRepository paquetesRepository;

    private final NecesidadService necesidadService;

    public Fachada() {
        this.depositoRepository = new InMemoryDepositosRepository();
        this.asignacionRepository = new InMemoryAsignacionesRepository();
        this.paquetesRepository = new InMemoryPaquetesRepository();
        this.necesidadService = new NecesidadService();
    }

    @Override
    public DepositoDTO agregarDeposito(DepositoDTO depositoDTO) {
        if (depositoDTO == null || depositoDTO.id() != null) {
            throw new RuntimeException("Deposito inválido");
        }
        String id = UUID.randomUUID().toString();

        Deposito deposito = new Deposito(
                id,
                null,
                depositoDTO.nombre(),
                depositoDTO.direccion(),
                depositoDTO.capacidadMaxima(),
                new ArrayList<>()
        );

        depositoRepository.save(deposito);

        return toDTO(deposito);
    }

    @Override
    public DepositoDTO buscarDepositoPorID(String depositoID) throws NoSuchElementException {

        val deposito = depositoRepository.findById(depositoID);

        if (deposito.isEmpty()) {
            throw new NoSuchElementException("No existe el depósito");
        }

        return toDTO(deposito.get());
    }

    public List<DepositoDTO> obtenerDepositos(){
        return this.depositoRepository.findAll().stream().map(this::toDTO).toList();
    }

    public DepositoDTO borrarDepositoPorID(String id){
        Deposito deposito = this.depositoRepository.deleteById(id);
        return toDTO(deposito);
    }

    @Override
    public AsignacionDTO buscarAsignacionPorPaqueteID(String paqueteID) throws NoSuchElementException {

        val asignacion = asignacionRepository.findAsignacionByPaqueteId(paqueteID);

        if (asignacion.isEmpty()) {
            throw new NoSuchElementException("No existe asignación");
        }
        return toDTO(asignacion.get());
    }

    public AsignacionDTO buscarAsignacionPorID(String id) throws NoSuchElementException {

        val asignacion = asignacionRepository.findById(id);
        if (asignacion.isEmpty()) {
            throw new NoSuchElementException("No existe asignación");
        }
        return toDTO(asignacion.get());
    }

    public PaqueteDTO buscarPaquetePorID(String id) throws NoSuchElementException {

        val paquete = paquetesRepository.findById(id);
        if (paquete.isEmpty()) {
            throw new NoSuchElementException("No existe asignación");
        }
        return toDTO(paquete.get());
    }

    @Override
    public DepositoDTO gestionarDonacion(String depositoID, String donacionID, String productoID, Integer cantidad)
            throws NoSuchElementException {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad inválida");
        }

        val depositoOptional = depositoRepository.findById(depositoID);


        if (depositoOptional.isEmpty()) {
            throw new NoSuchElementException("No existe el depósito");
        }
        val deposito = depositoOptional.get();

        List<NecesidadMaterialDTO> necesidadesDeProducto =
                fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe(productoID);


        if (necesidadesDeProducto.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades para este producto");
        }

        Paquete paquete = paquetesRepository.save(new Paquete(null, donacionID, productoID, cantidad));



        List<NecesidadMaterialDTO> necesidadesAplicables = necesidadesDeProducto.stream().filter(necesidadDeProducto ->
                this.necesidadService.esNecesidadAplicable(necesidadDeProducto, paquete.getCantidad())).toList();

        AsignacionDTO asignacionDTO = ejecutarMatchmaking(deposito.getId(), toDTO(paquete), necesidadesAplicables);

        //fachadaDonadoresYEntidades.satisfacerNecesidad(asignacionDTO.necesidadID(), paquete.cantidad());

        return toDTO(deposito); //raro que retorne deposito
    }

    @Override
    public void setAlgoritmoMM(String depositoID, TipoAlgoritmoEnum tipoAlgoritmo) {
        val depositoOptional = depositoRepository.findById(depositoID);
        if (depositoOptional.isEmpty()) {
            throw new NoSuchElementException();
        }
        val deposito = depositoOptional.get();
        deposito.setTipoAlgoritmo(tipoAlgoritmo);
    }

    @Override
    public AsignacionDTO ejecutarMatchmaking(String depositoID, PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidades) {
        if (paqueteDTO == null) {
            throw new RuntimeException("Paquete nulo");
        }
        if (necesidades.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades");
        }

        val depositoOptional = depositoRepository.findById(depositoID);
        if (depositoOptional.isEmpty()) {
            throw new NoSuchElementException();
        }
        val deposito = depositoOptional.get();

        Paquete paquete = toDomain(paqueteDTO);

        List<NecesidadLogistica> necesidadesLogistica = necesidades.stream().map(this::toDomain).toList();

        AlgoritmoAsignacion algoritmo = AlgoritmoFactory.crear(deposito.tipoAlgoritmo);
        NecesidadLogistica elegida = algoritmo.elegir(necesidadesLogistica, paquete.getCantidad());

        String asignacionID = UUID.randomUUID().toString();

        //if(elegida.getId() == null){
         //   elegida.setId("necesidad1");
       // }




        Asignacion asignacion = new Asignacion(
                asignacionID,
                paquete.getId(),
                elegida.getId(),
                LocalDateTime.now(),
                EstadoAsginacionEnum.ASIGNADA
        );

        asignacionRepository.save(asignacion);

        return toDTO(asignacion);
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

        val asignacionOptional = asignacionRepository.findAsignacionByPaqueteId(paqueteDTO.id());

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
                dto.id(),
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
                dto.id(),
                dto.donacionID(),
                dto.producto(),
                dto.cantidad()
        );
    }


    private Asignacion toDomain(AsignacionDTO dto) {
        return new Asignacion(
                dto.id(),
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
