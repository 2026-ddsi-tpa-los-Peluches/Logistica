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
                depositoDTO.nombre(),
                depositoDTO.direccion(),
                depositoDTO.capacidadMaxima()
        );

        depositos.put(id, deposito);

        return new DepositoDTO(
                id,
                depositoDTO.nombre(),
                depositoDTO.direccion(),
                depositoDTO.capacidadMaxima(),
                new ArrayList<>()
        );
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

        List<NecesidadDeEntidadDTO> necesidadDeProducto =
                fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe(productoID).
                        stream().map(this::mapear).toList();


        if (necesidadDeProducto.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades para este producto");
        }
        String paqueteID = UUID.randomUUID().toString();
        Paquete paquete = new Paquete(paqueteID, donacionID, productoID, cantidad); // no me gusta que paquete tenga donacionId

        AsignacionDTO asignacionDTO = ejecutarMatchmaking(toDTO(paquete), necesidadDeProducto);

        fachadaDonadoresYEntidades.satisfacerNecesidad(asignacionDTO.necesidadID(), paquete.cantidad());

        return toDTO(deposito); //raro que retorne deposito
    }

    @Override
    public void setAlgoritmoMM() {
        this.algoritmo = new AlgoritmoPrioridadSubatendidos();
    }

    @Override
    public AsignacionDTO ejecutarMatchmaking(PaqueteDTO paqueteDTO, List<NecesidadDeEntidadDTO> depositoDTO) {
        if (paqueteDTO == null) {
            throw new RuntimeException("Paquete nulo");
        }
        Paquete paquete = toDomain(paqueteDTO);

        List<NecesidadLogistica> necesidades =
                        depositoDTO
                        .stream()
                        .map(this::toDomain)
                        .toList();

        if (necesidades.isEmpty()) {
            throw new NoSuchElementException("No hay necesidades");
        }

        NecesidadLogistica elegida = algoritmo.elegir(necesidades);

        String asignacionID = UUID.randomUUID().toString();


        Asignacion asignacion = new Asignacion(
                asignacionID,
                paquete,
                elegida.id(),
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
                deposito.getNombre(),
                deposito.getDireccion(),
                deposito.getCapacidadMaxima(),
                new ArrayList<>() // por ahora vacío o mapeado después
        );
    }

    private PaqueteDTO toDTO(Paquete paquete){
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

    private Paquete toDomain(PaqueteDTO dto) {
        return new Paquete(
                dto.id(),
                dto.donacionID(),
                dto.producto(),
                dto.cantidad()
        );
    }


    private Asignacion toDomain(AsignacionDTO dto, Paquete paquete){
        return new Asignacion(
                dto.id(),
                paquete,
                dto.necesidadID(),
                dto.fecha(),
                dto.estado()
        );
    }

    private NecesidadLogistica toDomain(NecesidadDeEntidadDTO dto){
        return new NecesidadLogistica(
                dto.id(),
                dto.entidadID(),
                dto.cantidadObjetivo()
        );
    }



    // mapear

    private NecesidadDeEntidadDTO mapear(NecesidadMaterialDTO n) {
        return new NecesidadDeEntidadDTO(
                n.id(),
                n.entidadID(),
                n.nivelDeUrgencia(),
                n.descripcion(),
                n.cantidadObjetivo(),
                n.productoSolicitadoID()
        );
    }
}
