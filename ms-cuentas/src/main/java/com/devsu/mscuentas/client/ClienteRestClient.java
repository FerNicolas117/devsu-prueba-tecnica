package com.devsu.mscuentas.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Slf4j
public class ClienteRestClient {

    private final RestClient restClient;

    public ClienteRestClient(@Value("${ms-clientes.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String obtenerNombreCliente(String clienteId) {
        try {
            Map response = restClient.get()
                    .uri("/api/v1/clientes/by-cliente-id/{clienteId}", clienteId)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("nombre")) {
                return (String) response.get("nombre");
            }
            return null;
        } catch (Exception e) {
            log.warn("No se pudo obtener el nombre del cliente {}: {}", clienteId, e.getMessage());
            return null;
        }
    }
}
