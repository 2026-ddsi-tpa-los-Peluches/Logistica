package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.model.alogoritmos.AlgoritmoPrioridadSubatendidos;
import java.time.LocalDateTime;
import java.util.*;

public class Fachada implements FachadaLogistica {
    private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
    private FachadaDonaciones fachadaDonaciones;

    private Map<String, Deposito> depositos = new HashMap<>();
    private Map<String, Asignacion> asignacionesPorPaquete = new HashMap<>();
    private AlgoritmoAsignacion algoritmo = new AlgoritmoPrioridadSubatendidos();

    private Service service = new Service();

    public Fachada() {
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

        depositos.put(id, deposito);

        return toDTO(deposito);
    }

    @Override
    public DepositoDTO buscarDepositoPorID(String depositoID) throws NoSuchElementException {

        Deposito deposito = depositos.get(depositoID);

        if (deposito == null) {
            throw new NoSuchElementException("No existe el depósito");
        }

        return toDTO(deposito);
    }

    @Override
    public AsignacionDTO buscarAsignacionPorPaqueteID(String paqueteID) throws NoSuchElementException {

        Asignacion asignacion = asignacionesPorPaquete.get(paqueteID);

        if (asignacion == null) {
            throw new NoSuchElementException("No existe asignación");
        }
        return toDTO(asignacion);
    }

    @Override
    public DepositoDTO gestionarDonacion(String depositoID, String donacionID, String productoID, Integer cantidad)
            throws NoSuchElementException {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad inválida");
        }

        Deposito deposito = depositos.get(depositoID);

        if (deposito == null) {
            throw new NoSuchElementException("No existe el depósito");
        }


        List<NecesidadMaterialDTO> necesidadesDeProducto =
                fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe(productoID);


        if (necesidadesDeProducto.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades para este producto");
        }


        String paqueteID = UUID.randomUUID().toString();
        Paquete paquete = new Paquete(paqueteID, donacionID, productoID, cantidad); // no me gusta que paquete tenga donacionId


        List<NecesidadMaterialDTO> necesidadesAplicables = necesidadesDeProducto.stream().filter(necesidadDeProducto ->
                this.service.esNecesidadAplicable(necesidadDeProducto, paquete.cantidad())).toList();

        AsignacionDTO asignacionDTO = ejecutarMatchmaking(deposito.getId(), toDTO(paquete), necesidadesAplicables);

        //fachadaDonadoresYEntidades.satisfacerNecesidad(asignacionDTO.necesidadID(), paquete.cantidad());

        return toDTO(deposito); //raro que retorne deposito
    }

    @Override
    public void setAlgoritmoMM(String depositoID, TipoAlgoritmoEnum tipoAlgoritmo) {
        Deposito deposito = depositos.get(depositoID);
        if (deposito == null) {
            throw new NoSuchElementException();
        }
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

        Deposito deposito = depositos.get(depositoID);

        Paquete paquete = toDomain(paqueteDTO);

        List<NecesidadLogistica> necesidadesLogistica = necesidades.stream().map(this::toDomain).toList();

        AlgoritmoAsignacion algoritmo = AlgoritmoFactory.crear(deposito.tipoAlgoritmo);
        NecesidadLogistica elegida = algoritmo.elegir(necesidadesLogistica, paquete.cantidad());

        String asignacionID = UUID.randomUUID().toString();

        if(elegida.getId() == null){
            elegida.setId("necesidad1");
        }




        Asignacion asignacion = new Asignacion(
                asignacionID,
                paquete,
                elegida.getId(),
                LocalDateTime.now(),
                EstadoAsginacionEnum.ASIGNADA
        );

        asignacionesPorPaquete.put(paquete.paqueteID(), asignacion);

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

        Asignacion asignacion = asignacionesPorPaquete.get(paqueteDTO.id());

        if (asignacion == null) {
            throw new NoSuchElementException("No existe asignación para ese paquete");
        }

        fachadaDonadoresYEntidades.satisfacerNecesidad(
                asignacion.getNecesidadID(),
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
                paquete.paqueteID(),
                paquete.donacionID(),
                paquete.productoID(),
                paquete.cantidad()
        );
    }

    private AsignacionDTO toDTO(Asignacion asignacion) {

        return new AsignacionDTO(
                asignacion.getId(),
                asignacion.getPaquete().paqueteID(),
                asignacion.getNecesidadID(),
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
                dto.stockActual().stream().map(this::toDomain).toList()
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


    private Asignacion toDomain(AsignacionDTO dto, Paquete paquete) {
        return new Asignacion(
                dto.id(),
                paquete,
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
