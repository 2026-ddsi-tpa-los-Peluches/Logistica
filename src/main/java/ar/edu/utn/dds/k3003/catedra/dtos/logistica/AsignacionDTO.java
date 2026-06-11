package ar.edu.utn.dds.k3003.catedra.dtos.logistica;

import java.time.LocalDateTime;

public record AsignacionDTO(
    Integer id,
    Integer paqueteID,
    String necesidadID,
    LocalDateTime fecha,
    EstadoAsignacionEnum estado) {}
