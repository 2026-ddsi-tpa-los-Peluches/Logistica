package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.events.depositoEvents.*;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
@Service
public class DepositosPublisher {



    private final EventPublisher eventPublisher;

    public DepositosPublisher( EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notificarDepositoCreado(Deposito deposito) {
        String algoritmo = deposito.getTipoAlgoritmo() != null ? deposito.getTipoAlgoritmo().name() : "DEFAULT";

        DepositoCreadoEvent evento = new DepositoCreadoEvent(
                String.valueOf(deposito.getId()),
                deposito.getNombre(),
                deposito.getDireccion(),
                algoritmo,
                deposito.getCapacidadMaxima()
        );

        eventPublisher.publicar("deposito-creado", evento);
        System.out.println("Depósito creado enviado a RabbitMQ ID: " + deposito.getId());
    }

    public void notificarDepositoEliminado(Integer depositoId) {
        DepositoEliminadoEvent evento = new DepositoEliminadoEvent(String.valueOf(depositoId));
        eventPublisher.publicar("deposito-eliminado", evento);
        System.out.println("Depósito eliminado enviado a RabbitMQ ID: " + depositoId);
    }

    public void notificarAlgoritmoCambiado(Integer depositoId, TipoAlgoritmoEnum algoritmo) {
        String algoritmoStr = algoritmo != null ? algoritmo.toString() : "DEFAULT";

        DepositoAlgoritmoActualizadoEvent evento = new DepositoAlgoritmoActualizadoEvent(
                String.valueOf(depositoId),
                algoritmoStr
        );

        eventPublisher.publicar("deposito-algoritmo-actualizado", evento);
        System.out.println("Algoritmo de depósito actualizado enviado a RabbitMQ ID: " + depositoId);
    }

    public void notificarPaqueteAgregado(Integer depositoId, Paquete paquete) {
        DepositoPaqueteAgregadoEvent evento = new DepositoPaqueteAgregadoEvent(
                String.valueOf(depositoId),
                String.valueOf(paquete.getId()),
                paquete.getDonacionID(),
                paquete.getProductoID(),
                paquete.getCantidad()
        );
        eventPublisher.publicar("deposito-paquete-agregado", evento);
        System.out.println("Paquete agregado al stock del depósito enviado a RabbitMQ ID: " + paquete.getId());
    }

    public void notificarPaqueteStockActualizado(Integer depositoId, Paquete paquete, int cantidadDescontada) {
        DepositoPaqueteStockActualizadoEvent evento = new DepositoPaqueteStockActualizadoEvent(
                String.valueOf(depositoId),
                String.valueOf(paquete.getId()),
                paquete.getCantidad(), // La cantidad que le quedó al paquete en Postgres
                cantidadDescontada     // Lo que se descontó
        );

        eventPublisher.publicar("deposito-paquete-actualizado", evento);
        System.out.println("Stock de paquete actualizado enviado a RabbitMQ ID: " + paquete.getId());
    }

}
