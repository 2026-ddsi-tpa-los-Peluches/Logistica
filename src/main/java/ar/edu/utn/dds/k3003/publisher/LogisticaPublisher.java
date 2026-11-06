package ar.edu.utn.dds.k3003.publisher; // o tu paquete de servicios correspondiente

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.events.asignacionEvents.AsignacionActualizadaEvent;
import ar.edu.utn.dds.k3003.events.donadorEvents.DonacionDonadorEntregadaEvent;
import ar.edu.utn.dds.k3003.events.necesidadEvents.NecesidadAsignacionHecha;
import ar.edu.utn.dds.k3003.events.necesidadEvents.NecesidadAsignacionPendiente;
import ar.edu.utn.dds.k3003.events.productoEvents.StockProductoModificadoEvent;
import ar.edu.utn.dds.k3003.events.stockEvents.StockActualizadoEvent;
import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;

@Service
public class LogisticaPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final EventPublisher eventPublisher;

    public LogisticaPublisher(RabbitTemplate rabbitTemplate, EventPublisher eventPublisher) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventPublisher = eventPublisher;
    }

    public void notificarAsignacion(Asignacion asignacion, String entidadIdAsociada, String donacionIdAsociada, String depositoIdAsociado) {
        // Mapeamos LocalDateTime a Instant para que coincida con el record del Servicio de Consultas
        Instant fechaAsignacionInst = asignacion.getFecha() != null ?
                asignacion.getFecha().atZone(ZoneId.systemDefault()).toInstant() : Instant.now();

        Instant fechaEntregaInst = null;
        // Buscamos en el historial si hay una fecha de completada
        var historialCompletada = asignacion.getHistorialEstadoAsignaciones().stream()
                .filter(h -> h.getEstado().name().equals("COMPLETADA"))
                .findFirst();
        if (historialCompletada.isPresent()) {
            fechaEntregaInst = historialCompletada.get().getFecha().atZone(ZoneId.systemDefault()).toInstant();
        }

        AsignacionActualizadaEvent evento = new AsignacionActualizadaEvent(
                String.valueOf(asignacion.getId()),
                asignacion.getPaqueteId() != null ? String.valueOf(asignacion.getPaqueteId()) : null,
                asignacion.getNecesidadId(),
                depositoIdAsociado, // ID del depósito al que pertenece el paquete/asignación
                donacionIdAsociada, // ID de donación asociada al paquete
                entidadIdAsociada,  // ID de la entidad (viene de necesidades o del dominio)
                asignacion.getCantidad(),
                asignacion.getEstado() != null ? asignacion.getEstado().name() : "ASIGNADA",
                fechaAsignacionInst,
                null, // fechaIngresoDonacion (si la tenés a mano en el paquete, la pasás)
                fechaEntregaInst,
                Instant.now() // fechaEvento obligatoria para el Actualizador de Mongo
        );

        rabbitTemplate.convertAndSend("donatrack.eventos", "asignacion", evento);
    }

    public void notificarDonacionDonadorEntregada(Asignacion asignacion, Paquete paquete, DonacionDTO donacionDTO, NecesidadMaterialDTO necesidadMaterialDTO, EntidadBeneficaDTO entidadBeneficaDTO){
        DonacionDonadorEntregadaEvent evento = new DonacionDonadorEntregadaEvent(
                donacionDTO.donadorID(),
                necesidadMaterialDTO.entidadID(),
                entidadBeneficaDTO.razonSocial(),
                paquete.getCantidad()
        );

        eventPublisher.publicar("donador-entidad-donada", evento);
        System.out.println("Donación entregada enviada a RabbitMQ para donador: " + donacionDTO.donadorID());
    }

    public void notificarNecesidadAsignacionPendiente(Asignacion asignacion, Deposito deposito, int cantidadAAsignar){

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


    // STOCK

    public void notificarStockProductoModificado(Paquete paquete, Deposito deposito, int cantidadAAsignar) {
        StockProductoModificadoEvent evento = new StockProductoModificadoEvent(
                paquete.getProductoID(),
                String.valueOf(deposito.getId()),
                deposito.getNombre(),
                cantidadAAsignar
        );

        eventPublisher.publicar("producto-stock-modificado", evento);
        System.out.println("El stock de un porducto se ha modificado, se ha enviado a RabbitMQ para actualizarlo:  " +  paquete.getProductoID());
    }

}