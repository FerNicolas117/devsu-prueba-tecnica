package com.devsu.mscuentas.event;

import com.devsu.mscuentas.config.RabbitMQConfig;
import com.devsu.mscuentas.entity.Cuenta;
import com.devsu.mscuentas.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteEventListener {

    private final CuentaRepository cuentaRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    @Transactional
    public void consumirEvento(ClienteEvent evento) {
        log.info("Evento recibido: {} para cliente id: {}, nombre: {}",
                evento.getAccion(), evento.getClienteId(), evento.getNombre());

        // Actualizar el nombre del cliente en todas las cuentas.
        List<Cuenta> cuentas = cuentaRepository.findByClienteId(evento.getClienteId());

        if (!cuentas.isEmpty()) {
            cuentas.forEach(cuenta -> cuenta.setClienteNombre(evento.getNombre()));
            cuentaRepository.saveAll(cuentas);
            log.info("Nombre actualizado en {} cuentas del cliente id: {}",
                    cuentas.size(), evento.getClienteId());
        }
    }
}
