package ar.edu.utn.dds.k3003.controllers.requests;

public record AsignacionRequest(
        String depositoID,
        String donacionID,
        String productoID,
        Integer cantidad
) {
}