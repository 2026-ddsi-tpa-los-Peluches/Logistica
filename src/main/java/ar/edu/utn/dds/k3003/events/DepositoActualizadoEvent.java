package ar.edu.utn.dds.k3003.events;


import java.time.Instant;

public record DepositoActualizadoEvent(
        String depositoId, String nombre, int capacidad,
        String algoritmoMatchmaking, Instant fechaEvento) {}