package ar.edu.utn.dds.k3003.controllers.requests;

public record AsignacionRequest(
        Integer depositoID,
        String donacionID,
        String productoID,
        Integer cantidad
) {
}