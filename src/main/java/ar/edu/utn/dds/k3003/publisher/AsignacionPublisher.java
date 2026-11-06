package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsignacionEnum;
import ar.edu.utn.dds.k3003.events.asignacionEvents.AsignacionCreadaEvent;
import ar.edu.utn.dds.k3003.events.asignacionEvents.AsignacionEstadoEvent;
import ar.edu.utn.dds.k3003.model.AsgnacionRealizadaPor;
import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.model.Deposito;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AsignacionPublisher {

    private final EventPublisher eventPublisher;

    public AsignacionPublisher( EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notificarAsignacionCreada(Asignacion asignacion, Deposito deposito, String donacionID, NecesidadMaterialDTO necesidadMaterialDTO, AsgnacionRealizadaPor asgnacionRealizadaPor) {

        AsignacionCreadaEvent evento = new AsignacionCreadaEvent(
                String.valueOf(asignacion.getId()),
                asgnacionRealizadaPor.toString(),
                asignacion.getPaqueteId() != null ? String.valueOf(asignacion.getPaqueteId()) : null,
                asignacion.getNecesidadId(),
                deposito.getId().toString(),
                donacionID,
                necesidadMaterialDTO.entidadID(),
                asignacion.getCantidad(),
                asignacion.getEstado() != null ? asignacion.getEstado().name() : "ASIGNADA",
                asignacion.getFecha() != null ? asignacion.getFecha().atZone(java.time.ZoneId.systemDefault()).toInstant() : Instant.now(),
                Instant.now()
        );

        eventPublisher.publicar("asignacion-creada", evento);

        System.out.println("Asignacion creada enviada a RabbitMQ ID: " + asignacion.getId());
    }

    public void notificarAsignacionEstado(Integer asignacionID, EstadoAsignacionEnum estado) {
        AsignacionEstadoEvent evento = new AsignacionEstadoEvent(
                String.valueOf(asignacionID),
                estado.toString()
        );

        eventPublisher.publicar("asignacion-estado", evento);
        System.out.println("Evento de estado de asignación (" + estado + ") enviado a RabbitMQ para asignacion ID: " + asignacionID);
    }

}
