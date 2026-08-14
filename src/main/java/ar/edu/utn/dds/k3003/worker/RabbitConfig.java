package ar.edu.utn.dds.k3003.worker;

import ar.edu.utn.dds.k3003.componentes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.componentes.LogisticaClient;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${queue.host}")
    private String queueHost;

    @Value("${queue.username}")
    private String queueUsername;

    @Value("${queue.password}")
    private String queuePassword;

    @Value("${url.donadoresYEntidades}")
    private String urlDonadoresYEntidades;

    @Value("${url.logistica}")
    private String urlLogistica;

    // 1. Le enseñamos a Spring a crear el Channel de RabbitMQ
    @Bean
    public Channel rabbitChannel() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(queueHost);
        factory.setUsername(queueUsername);
        factory.setPassword(queuePassword);
        factory.setVirtualHost(queueUsername);

        Connection connection = factory.newConnection();
        return connection.createChannel();
    }

    // 2. Le enseñamos a Spring a crear el cliente de Donadores
    @Bean
    public DonadoresYEntidadesClient donadoresYEntidadesClient() {
        return new DonadoresYEntidadesClient(urlDonadoresYEntidades);
    }

    // 3. Le enseñamos a Spring a crear el cliente de Logística
    @Bean
    public LogisticaClient logisticaClient() {
        return new LogisticaClient(urlLogistica);
    }
}