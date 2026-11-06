package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.events.paqueteEvents.PaqueteCreadoEvent;
import ar.edu.utn.dds.k3003.events.paqueteEvents.PaqueteRestarCantidadEvent;
import ar.edu.utn.dds.k3003.model.Paquete;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PaquetePublisher {

    private final EventPublisher eventPublisher;

    public PaquetePublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notificarPaqueteCreado(Paquete paquete) {
        PaqueteCreadoEvent evento = new PaqueteCreadoEvent(
                paquete.getId() != null ? String.valueOf(paquete.getId()) : null,
                paquete.getDonacionID(),
                paquete.getProductoID(),
                paquete.getCantidad(),
                paquete.getDepositoID(),
                Instant.now()
        );

        eventPublisher.publicar("paquete-creado", evento);
        System.out.println("Paquete creado enviado a RabbitMQ para el ID: " + paquete.getId());
    }

    public void notificarPaqueteRestarCantidad(Paquete paquete, Integer cantidadARestar) {
        PaqueteRestarCantidadEvent evento = new PaqueteRestarCantidadEvent(
                paquete.getId() != null ? String.valueOf(paquete.getId()) : null,
                cantidadARestar,
                Instant.now()
        );

        eventPublisher.publicar("paquete-restar-cantidad", evento);
        System.out.println("Evento de restar cantidad enviado a RabbitMQ para el paquete ID: " + paquete.getId());
    }
}