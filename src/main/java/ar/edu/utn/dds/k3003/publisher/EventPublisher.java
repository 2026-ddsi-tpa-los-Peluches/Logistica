package ar.edu.utn.dds.k3003.publisher;

import ar.edu.utn.dds.k3003.config.RabbitServicioDeConsultasConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private final RabbitTemplate rabbit;

    public EventPublisher(RabbitTemplate rabbit) { this.rabbit = rabbit; }

    public void publicar(String claveDeRuta, Object evento) {
        rabbit.convertAndSend(RabbitServicioDeConsultasConfig.EXCHANGE, claveDeRuta, evento);
    }
}