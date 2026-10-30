package ar.edu.utn.dds.k3003.events;

import java.time.Instant;

public record AsignacionActualizadaEvent(
        String asignacionId, String paqueteId, String necesidadId,
        String depositoId, String donacionId, String entidadId,
        int unidades, String estado,
        Instant fechaAsignacion, Instant fechaIngresoDonacion,
        Instant fechaEntrega, Instant fechaEvento) {}