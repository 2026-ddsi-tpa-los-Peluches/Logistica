package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsginacionEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Asignacion {
    String id;
    String paqueteId;
    String necesidadId;
    LocalDateTime fecha;
    EstadoAsginacionEnum estado;
    private List<HistorialEstadoAsignacion> historialEstadoAsignaciones;


    public Asignacion(String id, String paquete, String necesidadID, LocalDateTime fecha, EstadoAsginacionEnum estado) {
        this.id = id;
        this.paqueteId = paquete;
        this.necesidadId = necesidadID;
        this.fecha = fecha;
        this.estado = estado;
        this.historialEstadoAsignaciones = new ArrayList<>();
    }


    //getters


    public String getId() {
        return id;
    }

    public String getPaqueteId() {
        return paqueteId;
    }

    public String getNecesidadId() {
        return necesidadId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoAsginacionEnum getEstado() {
        return estado;
    }



    public void completada(){
        this.estado = EstadoAsginacionEnum.COMPLETADA;
    }

    public void setId(String id) {
        this.id = id;
    }


}


