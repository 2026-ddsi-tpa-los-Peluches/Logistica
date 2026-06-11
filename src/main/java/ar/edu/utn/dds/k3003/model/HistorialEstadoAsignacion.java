package ar.edu.utn.dds.k3003.model;


import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsignacionEnum;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_estado_asignaciones")
public class HistorialEstadoAsignacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historialID;
    private EstadoAsignacionEnum estado;
    private LocalDateTime fecha;


    public HistorialEstadoAsignacion(){};

    public HistorialEstadoAsignacion(EstadoAsignacionEnum estado, LocalDateTime fecha) {
        this.estado = estado;
        this.fecha = fecha;
    }

    public EstadoAsignacionEnum getEstado() {
        return estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

