package com.devsu.msclientes.service.impl;

import com.devsu.msclientes.dto.ClienteRequestDto;
import com.devsu.msclientes.dto.ClienteResponseDto;
import com.devsu.msclientes.entity.Cliente;
import com.devsu.msclientes.entity.enums.Genero;
import com.devsu.msclientes.event.ClienteEvent;
import com.devsu.msclientes.event.ClienteEventPublisher;
import com.devsu.msclientes.exception.DuplicateResourceException;
import com.devsu.msclientes.exception.ResourceNotFoundException;
import com.devsu.msclientes.mapper.ClienteMapper;
import com.devsu.msclientes.repository.ClienteRepository;
import com.devsu.msclientes.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final ClienteEventPublisher eventPublisher;

    @Override
    public ClienteResponseDto crear(ClienteRequestDto dto) {
        // Validar que no exista otro cliente con la misma identificación.
        if (clienteRepository.existsByIdentificacion(dto.getIdentificacion())) {
            throw new DuplicateResourceException("Ya existe un cliente con identificación: " + dto.getIdentificacion());
        }

        Cliente cliente = clienteMapper.toEntity(dto);

        // Generar clienteId único de manera automática.
        cliente.setClienteId(generarClienteId());

        Cliente guardado = clienteRepository.save(cliente);

        // Publicar evento de creación.
        publicarEvento(guardado, "CREADO");

        return clienteMapper.toResponseDto(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerPorId(Long id) {
        Cliente cliente = buscarClientePorId(id);
        return clienteMapper.toResponseDto(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDto> obtenerTodos() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clienteMapper.toResponseDtoList(clientes);
    }

    @Override
    public ClienteResponseDto actualizar(Long id, ClienteRequestDto dto) {
        Cliente clienteExistente = buscarClientePorId(id);

        // Validar identificación duplicada si es que cambió.
        if (!clienteExistente.getIdentificacion().equals(dto.getIdentificacion())
            && clienteRepository.existsByIdentificacion(dto.getIdentificacion())) {
            throw new DuplicateResourceException(
                    "Ya existe un cliente con identificación: " + dto.getIdentificacion()
            );
        }

        clienteMapper.updateEntityFromDto(dto, clienteExistente);
        Cliente actualizado = clienteRepository.save(clienteExistente);

        // Publicar evento de actualización.
        publicarEvento(actualizado,  "ACTUALIZADO");

        return clienteMapper.toResponseDto(actualizado);
    }

    @Override
    public ClienteResponseDto actualizarParcial(Long id, Map<String, Object> campos) {
        Cliente cliente = buscarClientePorId(id);

        campos.forEach((campo, valor) -> {
            switch (campo) {
                case "nombre" -> cliente.setNombre((String) valor);
                case "genero" -> cliente.setGenero(Genero.valueOf(((String) valor).toUpperCase()));
                case "edad" -> cliente.setEdad((Integer) valor);
                case "direccion" -> cliente.setDireccion((String) valor);
                case "telefono" -> cliente.setTelefono((String) valor);
                case "contrasena" -> cliente.setContrasena((String) valor);
                case "estado" -> cliente.setEstado((Boolean) valor);
            }
        });

        Cliente actualizado = clienteRepository.save(cliente);

        // Publicar evento si cambió el nombre.
        if (campos.containsKey("nombre")) {
            publicarEvento(actualizado, "ACTUALIZADO");
        }

        return clienteMapper.toResponseDto(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Cliente cliente = buscarClientePorId(id);
        // clienteRepository.delete(cliente); -> Hard delete, es mejor emplear Soft delete.
        cliente.setEstado(false);
        clienteRepository.save(cliente);

        // Publicar evento de eliminación.
        publicarEvento(cliente, "ELIMINADO");
    }

    // Métodos privados.
    private Cliente buscarClientePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + id
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerPorClienteId(String clienteId) {
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con clienteId: " + clienteId));
        return clienteMapper.toResponseDto(cliente);
    }

    private String generarClienteId() {
        return UUID.randomUUID().toString().substring(0 ,8).toUpperCase();
    }

    private void publicarEvento(Cliente cliente, String accion) {
        ClienteEvent evento = ClienteEvent.builder()
                .clienteId(cliente.getClienteId())
                .nombre(cliente.getNombre())
                .accion(accion)
                .build();

        eventPublisher.publicarEvento(evento);
    }
}
