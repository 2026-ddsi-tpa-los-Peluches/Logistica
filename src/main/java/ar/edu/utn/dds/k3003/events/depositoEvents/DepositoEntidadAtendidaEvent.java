package ar.edu.utn.dds.k3003.events.depositoEvents;

import lombok.Getter;

import java.time.Instant;

@Getter
public class DepositoEntidadAtendidaEvent {
    private String depositoId;
    private String entidadId;
    private String razonSocial;
    private int unidadesEntregadas;
    private Instant fechaEvento;

    public DepositoEntidadAtendidaEvent(String depositoId, String entidadId, String razonSocial, int unidadesEntregadas) {
        this.depositoId = depositoId;
        this.entidadId = entidadId;
        this.razonSocial = razonSocial;
        this.unidadesEntregadas = unidadesEntregadas;
        this.fechaEvento = Instant.now();
    }
}