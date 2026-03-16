package com.devsu.mscuentas.controller;

import com.devsu.mscuentas.dto.MovimientoRequestDto;
import com.devsu.mscuentas.dto.MovimientoResponseDto;
import com.devsu.mscuentas.dto.MovimientoUpdateDto;
import com.devsu.mscuentas.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @PostMapping
    public ResponseEntity<MovimientoResponseDto> registrar(@Valid @RequestBody MovimientoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoService.registrar(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<MovimientoResponseDto>> obtenerTodos() {
        return ResponseEntity.ok(movimientoService.obtenerTodos());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MovimientoResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MovimientoUpdateDto dto) {
        return ResponseEntity.ok(movimientoService.actualizar(id, dto));
    }
}
