package com.devsu.mscuentas.service;

import com.devsu.mscuentas.dto.MovimientoRequestDto;
import com.devsu.mscuentas.dto.MovimientoResponseDto;
import com.devsu.mscuentas.dto.ReporteMovimientoDto;
import com.devsu.mscuentas.dto.ReporteResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface MovimientoService {

    MovimientoResponseDto registrar(MovimientoRequestDto dto);

    MovimientoResponseDto obtenerPorId(Long id);

    List<MovimientoResponseDto> obtenerTodos();

    MovimientoResponseDto actualizar(Long id, MovimientoRequestDto dto);

    //List<ReporteMovimientoDto> generarReporte(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin);

    ReporteResponseDto generarReporte(String clienteId, LocalDate fechaInicio, LocalDate fechaFin);
}
