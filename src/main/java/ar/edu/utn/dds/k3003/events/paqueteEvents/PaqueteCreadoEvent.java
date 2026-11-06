package ar.edu.utn.dds.k3003.events.paqueteEvents;

import java.time.Instant;

public record PaqueteCreadoEvent(
        String id,
        String donacionID,
        String productoID,
        Integer cantidad,
        Integer depositoID,
        Instant fechaEvento
) {}