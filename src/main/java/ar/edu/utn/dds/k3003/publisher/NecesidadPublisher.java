package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.events.necesidadEvents.NecesidadAsignacionHecha;
import ar.edu.utn.dds.k3003.events.necesidadEvents.NecesidadAsignacionPendiente;
import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.model.Deposito;
import org.springframework.stereotype.Component;

@Component
public class NecesidadPublisher {

    private final EventPublisher eventPublisher;

    public NecesidadPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notificarNecesidadAsignacionPendiente(Asignacion asignacion, Deposito deposito, int cantidadAAsignar) {
        NecesidadAsignacionPendiente evento = new NecesidadAsignacionPendiente(
                String.valueOf(asignacion.getId()),
                asignacion.getNecesidadId(),
                String.valueOf(deposito.getId()),
                cantidadAAsignar
        );
        eventPublisher.publicar("necesidad-asignacion-pendiente", evento);
        System.out.println("Asignación pendiente enviada a RabbitMQ para agregar a pendientes: " + asignacion.getId());
    }

    public void notificarNecesidadAsignacionHecha(Asignacion asignacion) {
        NecesidadAsignacionHecha evento = new NecesidadAsignacionHecha(
                String.valueOf(asignacion.getId()),
                asignacion.getNecesidadId()
        );

        eventPublisher.publicar("necesidad-asignacion-hecha", evento);
        System.out.println("Asignación completada enviada a RabbitMQ para remover de pendientes: " + asignacion.getId());
    }
}