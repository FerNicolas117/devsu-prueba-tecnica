package com.devsu.msclientes.mapper;

import com.devsu.msclientes.dto.ClienteRequestDto;
import com.devsu.msclientes.dto.ClienteResponseDto;
import com.devsu.msclientes.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    // Request DTO -> Entity.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clienteId", ignore = true) // Se genera en el Service.
    Cliente toEntity(ClienteRequestDto dto);

    // Entity -> Response DTO.
    ClienteResponseDto toResponseDto(Cliente entity);

    // Lista de entities -> Lista de response DTO.
    List<ClienteResponseDto> toResponseDtoList(List<Cliente> entities);

    // Actualizar entity existente con datos del DTO (para PUT/PATCH).
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clienteId", ignore = true)
    void updateEntityFromDto(ClienteRequestDto dto, @MappingTarget Cliente entity);
}
