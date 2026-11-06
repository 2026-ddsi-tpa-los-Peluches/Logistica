package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.events.productoEvents.StockProductoModificadoEvent;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
import org.springframework.stereotype.Component;

@Component
public class ProductoPublisher {

    private final EventPublisher eventPublisher;

    public ProductoPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notificarStockProductoModificado(Paquete paquete, Deposito deposito, int cantidadAAsignar) {
        StockProductoModificadoEvent evento = new StockProductoModificadoEvent(
                paquete.getProductoID(),
                String.valueOf(deposito.getId()),
                deposito.getNombre(),
                cantidadAAsignar
        );

        eventPublisher.publicar("producto-stock-modificado", evento);
        System.out.println("El stock de un producto se ha modificado, se ha enviado a RabbitMQ para actualizarlo: " + paquete.getProductoID());
    }
}