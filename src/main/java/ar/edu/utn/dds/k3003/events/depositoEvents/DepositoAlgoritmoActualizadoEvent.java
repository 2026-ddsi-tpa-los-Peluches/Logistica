package ar.edu.utn.dds.k3003.events.depositoEvents;

import lombok.Getter;

import java.time.Instant;


@Getter
public class DepositoAlgoritmoActualizadoEvent {
    private String depositoId;
    private String algoritmoMatchmaking;
    private Instant fechaEvento;

    public DepositoAlgoritmoActualizadoEvent(String depositoId, String algoritmoMatchmaking) {
        this.depositoId = depositoId;
        this.algoritmoMatchmaking = algoritmoMatchmaking;
        this.fechaEvento = Instant.now();
    }
}