package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsignacionEnum;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "asignaciones")
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    Integer paqueteId;
    String necesidadId;
    LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    AsgnacionRealizadaPor formaDeAsignacion;

    @Enumerated(EnumType.STRING)
    EstadoAsignacionEnum estado;

    @OneToMany(cascade = CascadeType.ALL)
    private List<HistorialEstadoAsignacion> historialEstadoAsignaciones;


    public Asignacion(){}

    public Asignacion(Integer paquete, String necesidadID, LocalDateTime fecha, EstadoAsignacionEnum estado, AsgnacionRealizadaPor formaDeAsignacion) {
        this.paqueteId = paquete;
        this.necesidadId = necesidadID;
        this.fecha = fecha;
        this.estado = estado;
        this.historialEstadoAsignaciones = new ArrayList<>();

        this.historialEstadoAsignaciones.add(
                new HistorialEstadoAsignacion(
                        estado,
                        fecha
                )
        );
        this.formaDeAsignacion = formaDeAsignacion;
    }


    //getters


    public AsgnacionRealizadaPor getFormaDeAsignacion() {return formaDeAsignacion;}

    public Integer getId() {
        return id;
    }

    public Integer getPaqueteId() {
        return paqueteId;
    }

    public List<HistorialEstadoAsignacion> getHistorialEstadoAsignaciones() {
        return historialEstadoAsignaciones;
    }

    public String getNecesidadId() {
        return necesidadId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoAsignacionEnum getEstado() {
        return estado;
    }



    public void completada(){
        this.estado = EstadoAsignacionEnum.COMPLETADA;

        historialEstadoAsignaciones.add(
                new HistorialEstadoAsignacion(
                        this.getEstado(),
                        LocalDateTime.now())
        );
    }



    public void setId(Integer id) {
        this.id = id;
    }
}


