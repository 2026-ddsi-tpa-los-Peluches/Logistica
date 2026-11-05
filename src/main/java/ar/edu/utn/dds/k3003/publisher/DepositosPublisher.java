package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.events.depositoEvents.DepositoAlgoritmoActualizadoEvent;
import ar.edu.utn.dds.k3003.events.depositoEvents.DepositoCreadoEvent;
import ar.edu.utn.dds.k3003.events.depositoEvents.DepositoEliminadoEvent;
import ar.edu.utn.dds.k3003.model.Deposito;
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
}
