package ar.edu.utn.dds.k3003.controllers.requests;


public record DepositoRequest(
        String nombre,
        String direccion,
        Integer capacidadMaxima
) {
}