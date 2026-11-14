package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.events.donadorEvents.DonacionDonadorEntregadaEvent;
import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.model.Paquete;
import org.springframework.stereotype.Component;

@Component
public class DonadorPublisher {

    private final EventPublisher eventPublisher;

    public DonadorPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notificarDonacionDonadorEntregada(Asignacion asignacion, DonacionDTO donacionDTO, NecesidadMaterialDTO necesidadMaterialDTO, EntidadBeneficaDTO entidadBeneficaDTO) {
        DonacionDonadorEntregadaEvent evento = new DonacionDonadorEntregadaEvent(
                donacionDTO.donadorID(),
                necesidadMaterialDTO.entidadID(),
                entidadBeneficaDTO.razonSocial(),
                asignacion.getCantidad()
        );

        eventPublisher.publicar("donador-entidad-donada", evento);
        System.out.println("Donación entregada enviada a RabbitMQ para donador: " + donacionDTO.donadorID());
    }
}