package ar.edu.utn.dds.k3003.events;

import lombok.Getter;

import java.time.Instant;


@Getter
public class NecesidadAsignacionPendiente {
    private String necesidadId;
    private String asignacionId;
    private String depositoId;
    private int unidades;
    private Instant fechaEvento;

    public NecesidadAsignacionPendiente(String necesidadId, String asignacionId, String depositoId, int unidades) {
        this.necesidadId = necesidadId;
        this.asignacionId = asignacionId;
        this.depositoId = depositoId;
        this.unidades = unidades;
        this.fechaEvento = Instant.now();
    }
}