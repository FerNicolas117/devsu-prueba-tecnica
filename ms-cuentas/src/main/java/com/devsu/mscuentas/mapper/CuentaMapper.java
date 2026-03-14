package com.devsu.mscuentas.mapper;

import com.devsu.mscuentas.dto.CuentaRequestDto;
import com.devsu.mscuentas.dto.CuentaResponseDto;
import com.devsu.mscuentas.entity.Cuenta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CuentaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "saldoDisponible", source = "saldoInicial")
    @Mapping(target = "clienteNombre", ignore = true)
    @Mapping(target = "movimientos", ignore = true)
    Cuenta toEntity(CuentaRequestDto dto);

    CuentaResponseDto toResponseDto(Cuenta entity);

    List<CuentaResponseDto> toResponseDtoList(List<Cuenta> entities);
}
