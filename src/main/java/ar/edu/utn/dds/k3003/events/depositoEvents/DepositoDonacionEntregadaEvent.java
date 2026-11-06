package ar.edu.utn.dds.k3003.events.depositoEvents;

import lombok.Getter;

import java.time.Instant;



@Getter
public class DepositoDonacionEntregadaEvent {
    private String depositoId;
    private String donacionId;
    private Instant fechaQueSeEntrego;
    private Instant fechaEvento;

    public DepositoDonacionEntregadaEvent(String depositoId, String donacionId, Instant fechaQueSeEntrego) {
        this.depositoId = depositoId;
        this.donacionId = donacionId;
        this.fechaQueSeEntrego = fechaQueSeEntrego;
        this.fechaEvento = Instant.now();
    }
}