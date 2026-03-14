package com.devsu.msclientes.event;

import com.devsu.msclientes.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicarEvento(ClienteEvent evento) {
        log.info("Publicando evento: {} para cliente id: {}", evento.getAccion(), evento.getClienteId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                evento
        );
    }
}
