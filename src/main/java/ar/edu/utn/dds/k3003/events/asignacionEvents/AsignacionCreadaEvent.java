package ar.edu.utn.dds.k3003.events.asignacionEvents;

import java.time.Instant;

public record AsignacionCreadaEvent(
        String asignacionId, String asignacionRealizadaPor ,String paqueteId, String necesidadId,
        String depositoId, String donacionId, String entidadId,
        int unidades, String estado,
        Instant fechaAsignacion, Instant fechaEvento) {}