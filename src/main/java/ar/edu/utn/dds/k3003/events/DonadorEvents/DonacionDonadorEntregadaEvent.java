package ar.edu.utn.dds.k3003.events.DonadorEvents;

import lombok.Getter;

import javax.annotation.processing.Generated;
import java.time.Instant;

@Getter
public class DonacionDonadorEntregadaEvent {
    private String donadorId;
    private String entidadId;
    private String razonSocialEntidad;
    private Integer cantidad;
    private Instant fechaEvento;

    public DonacionDonadorEntregadaEvent(String donadorId, String entidadId, String razonSocialEntidad, Integer cantidad) {
        this.donadorId = donadorId;
        this.entidadId = entidadId;
        this.razonSocialEntidad = razonSocialEntidad;
        this.cantidad = cantidad;
        this.fechaEvento = Instant.now();
    }
}