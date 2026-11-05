package ar.edu.utn.dds.k3003.events.depositoEvents;

import lombok.Getter;

import java.time.Instant;



@Getter
public class DepositoCreadoEvent {
    private String depositoId;
    private String nombre;
    private String direccion;
    private String algoritmoMatchmaking;
    private Integer capacidadMaxima;
    private Instant fechaEvento;

    public DepositoCreadoEvent(String depositoId, String nombre, String direccion, String algoritmoMatchmaking, Integer capacidadMaxima) {
        this.depositoId = depositoId;
        this.nombre = nombre;
        this.direccion = direccion;
        this.algoritmoMatchmaking = algoritmoMatchmaking;
        this.capacidadMaxima = capacidadMaxima;
        this.fechaEvento = Instant.now();
    }
}