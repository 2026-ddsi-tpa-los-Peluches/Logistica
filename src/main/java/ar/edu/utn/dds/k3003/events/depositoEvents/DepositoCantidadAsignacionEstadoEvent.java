package ar.edu.utn.dds.k3003.events.depositoEvents;


import lombok.Getter;

import java.time.Instant;

@Getter
public class DepositoCantidadAsignacionEstadoEvent {
    private String depositoId;
    private String estado; // "ASIGNADA" o "COMPLETADA"
    private Instant fechaEvento;

    public DepositoCantidadAsignacionEstadoEvent(String depositoId, String estado) {
        this.depositoId = depositoId;
        this.estado = estado;
        this.fechaEvento = Instant.now();
    }
}