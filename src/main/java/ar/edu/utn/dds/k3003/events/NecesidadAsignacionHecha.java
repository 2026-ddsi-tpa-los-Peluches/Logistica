package ar.edu.utn.dds.k3003.events;

import lombok.Getter;

import java.time.Instant;


@Getter
public class NecesidadAsignacionHecha {
    private String asignacionId;
    private String necesidadId;
    private Instant fechaEvento;

    public NecesidadAsignacionHecha(String asignacionId, String necesidadId) {
        this.asignacionId = asignacionId;
        this.necesidadId = necesidadId;
        this.fechaEvento = Instant.now();
    }


}