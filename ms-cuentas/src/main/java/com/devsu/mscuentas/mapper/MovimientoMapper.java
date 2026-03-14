package com.devsu.mscuentas.mapper;

import com.devsu.mscuentas.dto.MovimientoResponseDto;
import com.devsu.mscuentas.entity.Movimiento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MovimientoMapper {

    @Mapping(source = "cuenta.numeroCuenta", target = "numeroCuenta")
    @Mapping(source = "cuenta.tipoCuenta", target = "tipoCuenta")
    MovimientoResponseDto toResponseDto(Movimiento entity);

    List<MovimientoResponseDto> toResponseDtoList(List<Movimiento> entities);
}
