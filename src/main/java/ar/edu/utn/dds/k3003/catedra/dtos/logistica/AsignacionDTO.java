package ar.edu.utn.dds.k3003.catedra.dtos.logistica;

import java.time.LocalDateTime;

public record AsignacionDTO(
    Integer id,
    String donacionId,
    String necesidadID,
    LocalDateTime fecha,
    EstadoAsignacionEnum estado,
    Integer cantidad) {}
