package ar.edu.utn.dds.k3003.events.paqueteEvents;

import java.time.Instant;

public record PaqueteRestarCantidadEvent(
        String id,
        Integer cantidadARestar,
        Instant fechaEvento
) {}