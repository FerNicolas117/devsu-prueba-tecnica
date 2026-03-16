package com.devsu.mscuentas.service;

import com.devsu.mscuentas.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface MovimientoService {

    MovimientoResponseDto registrar(MovimientoRequestDto dto);

    MovimientoResponseDto obtenerPorId(Long id);

    List<MovimientoResponseDto> obtenerTodos();

    MovimientoResponseDto actualizar(Long id, MovimientoUpdateDto dto);

    //List<ReporteMovimientoDto> generarReporte(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin);

    ReporteResponseDto generarReporte(String clienteId, LocalDate fechaInicio, LocalDate fechaFin);
}
