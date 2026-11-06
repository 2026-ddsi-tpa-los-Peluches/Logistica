package ar.edu.utn.dds.k3003.events.asignacionEvents;


import lombok.Getter;

import java.time.Instant;

@Getter
public class AsignacionEstadoEvent {
    private String asignacionID;
    private String estado; // "ASIGNADA" o "COMPLETADA"
    private Instant fechaEvento;

    public AsignacionEstadoEvent(String asignacionID, String estado) {
        this.asignacionID = asignacionID;
        this.estado = estado;
        this.fechaEvento = Instant.now();
    }
}