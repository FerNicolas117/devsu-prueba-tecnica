package com.devsu.msclientes.service;

import com.devsu.msclientes.dto.ClienteRequestDto;
import com.devsu.msclientes.dto.ClienteResponseDto;

import java.util.List;
import java.util.Map;

public interface ClienteService {

    ClienteResponseDto crear(ClienteRequestDto dto);

    ClienteResponseDto obtenerPorId(Long id);

    List<ClienteResponseDto> obtenerTodos();

    ClienteResponseDto actualizar(Long id, ClienteRequestDto dto);

    ClienteResponseDto actualizarParcial(Long id, Map<String, Object> campos);

    void eliminar(Long id);

    ClienteResponseDto obtenerPorClienteId(String clienteId);
}
