package ar.edu.utn.dds.k3003.catedra.dtos.donaciones;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;

public record DonacionMensajeDTO (
        String donacionID,
        Integer depositoID,
        int cantidadDonada,
        TipoAlgoritmoEnum tipoAlgoritmo,
        String productoID
){}

//(Integer depositoID, String donacionID, String productoID, Integer cantidadDonada
