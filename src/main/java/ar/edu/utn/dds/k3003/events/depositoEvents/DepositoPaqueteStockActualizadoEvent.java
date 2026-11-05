package ar.edu.utn.dds.k3003.events.depositoEvents;


import lombok.Getter;

import java.time.Instant;



@Getter
public class DepositoPaqueteStockActualizadoEvent {
    private String depositoId;
    private String paqueteId;
    private int nuevaCantidad; // La cantidad restante que le quedó al paquete
    private int cantidadDescontada; // Lo que se restó en esta operación (para el stockTotal)
    private Instant fechaEvento;

    public DepositoPaqueteStockActualizadoEvent(String depositoId, String paqueteId, int nuevaCantidad, int cantidadDescontada) {
        this.depositoId = depositoId;
        this.paqueteId = paqueteId;
        this.nuevaCantidad = nuevaCantidad;
        this.cantidadDescontada = cantidadDescontada;
        this.fechaEvento = Instant.now();
    }
}