package ar.edu.utn.dds.k3003.events.stockEvents;

import java.time.Instant;

public record StockActualizadoEvent(
        String productoId, String depositoId, String nombreDeposito,
        int cantidad, Instant fechaEvento) {}