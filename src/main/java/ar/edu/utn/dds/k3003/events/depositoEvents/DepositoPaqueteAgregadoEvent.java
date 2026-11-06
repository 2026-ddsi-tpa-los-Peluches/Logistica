package ar.edu.utn.dds.k3003.events.depositoEvents;


import lombok.Getter;

import java.time.Instant;


@Getter
public class DepositoPaqueteAgregadoEvent {
    private String depositoId;
    private String paqueteId;
    private String donacionID;
    private String productoID;
    private int cantidad;
    private Instant fechaEvento;

    public DepositoPaqueteAgregadoEvent(String depositoId, String paqueteId, String donacionID, String productoID, int cantidad) {
        this.depositoId = depositoId;
        this.paqueteId = paqueteId;
        this.donacionID = donacionID;
        this.productoID = productoID;
        this.cantidad = cantidad;
        this.fechaEvento = Instant.now();
    }
}