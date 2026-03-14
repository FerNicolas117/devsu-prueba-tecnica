package com.devsu.mscuentas.service;

import com.devsu.mscuentas.dto.CuentaRequestDto;
import com.devsu.mscuentas.dto.CuentaResponseDto;

import java.util.List;

public interface CuentaService {

    CuentaResponseDto crear(CuentaRequestDto dto);

    CuentaResponseDto obtenerPorId(Long id);

    List<CuentaResponseDto> obtenerTodas();

    CuentaResponseDto actualizar(Long id, CuentaRequestDto dto);

    void eliminar(Long id);
}
