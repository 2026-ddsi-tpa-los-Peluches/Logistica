package ar.edu.utn.dds.k3003.events.productoEvents;

import lombok.Getter;

import java.time.Instant;

@Getter
public class StockProductoModificadoEvent {
    private String productoId;
    private String depositoId;
    private String nombreDeposito;
    private int cantidadDelta; // Puede ser positivo (entra stock) o negativo (sale stock)
    private Instant fechaEvento;

    public StockProductoModificadoEvent(String productoId, String depositoId, String nombreDeposito, int cantidadDelta) {
        this.productoId = productoId;
        this.depositoId = depositoId;
        this.nombreDeposito = nombreDeposito;
        this.cantidadDelta = cantidadDelta;
        this.fechaEvento = Instant.now();
    }

}