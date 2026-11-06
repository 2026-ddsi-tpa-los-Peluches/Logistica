package ar.edu.utn.dds.k3003.publisher; // o tu paquete de servicios correspondiente

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsignacionEnum;
import ar.edu.utn.dds.k3003.events.asignacionEvents.AsignacionCreadaEvent;
import ar.edu.utn.dds.k3003.events.asignacionEvents.AsignacionEstadoEvent;
import ar.edu.utn.dds.k3003.events.depositoEvents.DepositoCantidadAsignacionEstadoEvent;
import ar.edu.utn.dds.k3003.events.donadorEvents.DonacionDonadorEntregadaEvent;
import ar.edu.utn.dds.k3003.events.necesidadEvents.NecesidadAsignacionHecha;
import ar.edu.utn.dds.k3003.events.necesidadEvents.NecesidadAsignacionPendiente;
import ar.edu.utn.dds.k3003.events.productoEvents.StockProductoModificadoEvent;
import ar.edu.utn.dds.k3003.events.stockEvents.StockActualizadoEvent;
import ar.edu.utn.dds.k3003.model.AsgnacionRealizadaPor;
import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;

@Service
public class LogisticaPublisher {

    private final EventPublisher eventPublisher;

    public LogisticaPublisher(EventPublisher eventPublisher) {

        this.eventPublisher = eventPublisher;
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