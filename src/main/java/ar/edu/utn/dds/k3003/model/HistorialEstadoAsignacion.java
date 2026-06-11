package ar.edu.utn.dds.k3003.model;


import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsginacionEnum;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_estado_asignaciones")
public class HistorialEstadoAsignacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historialID;
    private EstadoAsginacionEnum estado;
    private LocalDateTime fecha;


    public HistorialEstadoAsignacion(){};

    public HistorialEstadoAsignacion(EstadoAsginacionEnum estado, LocalDateTime fecha) {
        this.estado = estado;
        this.fecha = fecha;
    }

    public EstadoAsginacionEnum getEstado() {
        return estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}

