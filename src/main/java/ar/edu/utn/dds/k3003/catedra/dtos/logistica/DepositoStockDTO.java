package ar.edu.utn.dds.k3003.catedra.dtos.logistica;

import java.time.LocalDateTime;

public record DepositoStockDTO(
        Integer depositoId,
        Integer stockActualId
) {}