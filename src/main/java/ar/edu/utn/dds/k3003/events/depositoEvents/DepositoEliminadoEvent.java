// DepositoEliminadoEvent.java
package ar.edu.utn.dds.k3003.events.depositoEvents;

import lombok.Getter;

import java.time.Instant;

@Getter
public class DepositoEliminadoEvent {
    private String depositoId;
    private Instant fechaEvento;

    public DepositoEliminadoEvent(String depositoId) {
        this.depositoId = depositoId;
        this.fechaEvento = Instant.now();
    }
}