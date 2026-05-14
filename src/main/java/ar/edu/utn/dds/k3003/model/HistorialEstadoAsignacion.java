package ar.edu.utn.dds.k3003.model;


import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsginacionEnum;

import java.time.LocalDateTime;

public class HistorialEstadoAsignacion {
    private String asignacionID;
    private EstadoAsginacionEnum estado;
    private LocalDateTime fecha;

    public HistorialEstadoAsignacion(String asignacionID, EstadoAsginacionEnum estado, LocalDateTime fecha) {
        this.asignacionID = asignacionID;
        this.estado = estado;
        this.fecha = fecha;
    }

    public String getAsignacionID() {
        return asignacionID;
    }

    public EstadoAsginacionEnum getEstado() {
        return estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

