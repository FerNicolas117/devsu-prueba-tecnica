package com.devsu.mscuentas.controller;

import com.devsu.mscuentas.dto.ReporteMovimientoDto;
import com.devsu.mscuentas.dto.ReporteResponseDto;
import com.devsu.mscuentas.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final MovimientoService movimientoService;

    // GET /api/v1/reportes?clienteId=1&fechaInicio=2026-03-01&fechaFin=2026-03-12
    @GetMapping
    public ResponseEntity<ReporteResponseDto> generarReporte(
            @RequestParam String clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        ReporteResponseDto reporte = movimientoService
                .generarReporte(clienteId, fechaInicio, fechaFin);

        return ResponseEntity.ok(reporte);
    }
}
