package ar.edu.utn.dds.k3003.events.depositoEvents;


import lombok.Getter;

import java.time.Instant;


@Getter
public class DepositoDonacionIngresadaEvent {
    private String depositoId;
    private String donacionId;
    private String donadorId;
    private int cantidadDonada;
    private Instant fechaQueLlego;
    private Instant fechaEvento;

    public DepositoDonacionIngresadaEvent(String depositoId, String donacionId, String donadorId, int cantidadDonada, Instant fechaQueLlego) {
        this.depositoId = depositoId;
        this.donacionId = donacionId;
        this.donadorId = donadorId;
        this.cantidadDonada = cantidadDonada;
        this.fechaQueLlego = fechaQueLlego;
        this.fechaEvento = Instant.now();
    }

}