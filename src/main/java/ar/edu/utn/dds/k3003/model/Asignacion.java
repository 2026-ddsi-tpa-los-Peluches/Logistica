package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsignacionEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "asignaciones")
public class Asignacion {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    Integer paqueteId;
    String necesidadId;
    LocalDateTime fecha;
    @Setter
    Integer cantidad;

    @Enumerated(EnumType.STRING)
    AsgnacionRealizadaPor formaDeAsignacion;

    @Enumerated(EnumType.STRING)
    EstadoAsignacionEnum estado;

    @OneToMany(cascade = CascadeType.ALL)
    private List<HistorialEstadoAsignacion> historialEstadoAsignaciones;

    public Asignacion() {
    }

    public Asignacion(
            Integer paqueteId,
            String necesidadId,
            LocalDateTime fecha,
            EstadoAsignacionEnum estado,
            AsgnacionRealizadaPor formaDeAsignacion,
            Integer cantidad
    ) {
        this.paqueteId = paqueteId;
        this.necesidadId = necesidadId;
        this.fecha = fecha;
        this.estado = estado;
        this.formaDeAsignacion = formaDeAsignacion;
        this.cantidad = cantidad;

        this.historialEstadoAsignaciones = new ArrayList<>();

        this.historialEstadoAsignaciones.add(
                new HistorialEstadoAsignacion(
                        estado,
                        fecha
                )
        );
    }

    public void completada() {
        this.estado = EstadoAsignacionEnum.COMPLETADA;

        historialEstadoAsignaciones.add(
                new HistorialEstadoAsignacion(
                        this.estado,
                        LocalDateTime.now()
                )
        );
    }

}