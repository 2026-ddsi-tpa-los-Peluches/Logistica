package ar.edu.utn.dds.k3003.publisher; // o tu paquete de servicios correspondiente

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.events.AsignacionActualizadaEvent;
import ar.edu.utn.dds.k3003.events.DepositoActualizadoEvent;
import ar.edu.utn.dds.k3003.events.DonadorEvents.DonacionDonadorEntregadaEvent;
import ar.edu.utn.dds.k3003.events.StockActualizadoEvent;
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

    public void notificarDeposito(Deposito deposito) {
        DepositoActualizadoEvent evento = new DepositoActualizadoEvent(
                String.valueOf(deposito.getId()),
                deposito.getNombre(),
                deposito.getCapacidadMaxima(),
                deposito.getTipoAlgoritmo() != null ? deposito.getTipoAlgoritmo().name() : "DEFAULT",
                Instant.now()
        );

        rabbitTemplate.convertAndSend("donatrack.eventos", "deposito", evento);
    }

    public void notificarStock(Deposito deposito, Paquete paquete) {
        StockActualizadoEvent evento = new StockActualizadoEvent(
                paquete.getProductoID(),
                String.valueOf(deposito.getId()),
                deposito.getNombre(),
                paquete.getCantidad(),
                Instant.now()
        );

        // Routing key exacta para stock que espera tu ConsultasListener ("consultas.stock")
        rabbitTemplate.convertAndSend("donatrack.eventos", "stock", evento);
    }

    public void notificarDonacionDonadorEntregada(Asignacion asignacion, Paquete paquete, DonacionDTO donacionDTO, NecesidadMaterialDTO necesidadMaterialDTO, EntidadBeneficaDTO entidadBeneficaDTO){
        DonacionDonadorEntregadaEvent evento = new DonacionDonadorEntregadaEvent(
                donacionDTO.donadorID(),
                necesidadMaterialDTO.entidadID(),
                entidadBeneficaDTO.razonSocial(),
                paquete.getCantidad()
        );

        eventPublisher.publicar("donador-entidad-donada-exchange", evento);
        System.out.println("Donación entregada enviada a RabbitMQ para donador: " + donacionDTO.donadorID());
    }
}