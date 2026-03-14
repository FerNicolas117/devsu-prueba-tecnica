package com.devsu.mscuentas.controller;

import com.devsu.mscuentas.dto.CuentaRequestDto;
import com.devsu.mscuentas.dto.CuentaResponseDto;
import com.devsu.mscuentas.service.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<CuentaResponseDto> crear(@Valid @RequestBody CuentaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaService.crear(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponseDto>> obtenerTodas() {
        return ResponseEntity.ok(cuentaService.obtenerTodas());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaResponseDto> actualizar(
            @PathVariable Long id, @Valid @RequestBody CuentaRequestDto dto) {
        return ResponseEntity.ok(cuentaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cuentaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
