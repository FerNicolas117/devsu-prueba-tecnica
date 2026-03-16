package com.devsu.mscuentas.service.impl;

import com.devsu.mscuentas.client.ClienteRestClient;
import com.devsu.mscuentas.dto.CuentaRequestDto;
import com.devsu.mscuentas.dto.CuentaResponseDto;
import com.devsu.mscuentas.entity.Cuenta;
import com.devsu.mscuentas.exception.DuplicateResourceException;
import com.devsu.mscuentas.exception.ResourceNotFoundException;
import com.devsu.mscuentas.mapper.CuentaMapper;
import com.devsu.mscuentas.repository.CuentaRepository;
import com.devsu.mscuentas.repository.MovimientoRepository;
import com.devsu.mscuentas.service.CuentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final CuentaMapper cuentaMapper;
    private final ClienteRestClient clienteRestClient;

    @Override
    public CuentaResponseDto crear(CuentaRequestDto dto) {
        if (cuentaRepository.existsByNumeroCuenta(dto.getNumeroCuenta())) {
            throw new DuplicateResourceException(
                    "Ya existe una cuenta con número: " + dto.getNumeroCuenta());
        }

        Cuenta cuenta = cuentaMapper.toEntity(dto);

        // Obtener nombre del cliente desde ms-clientes síncrona.
        String nombreCliente = clienteRestClient.obtenerNombreCliente(dto.getClienteId());
        cuenta.setClienteNombre(nombreCliente);
        log.info("Nombre: {} asigando a la cuenta: {}", nombreCliente, dto.getNumeroCuenta());

        Cuenta guardada = cuentaRepository.save(cuenta);
        return cuentaMapper.toResponseDto(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponseDto obtenerPorId(Long id) {
        Cuenta cuenta = buscarCuentaPorId(id);
        return cuentaMapper.toResponseDto(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponseDto> obtenerTodas() {
        return cuentaMapper.toResponseDtoList(cuentaRepository.findAll());
    }

    @Override
    public CuentaResponseDto actualizar(Long id, CuentaRequestDto dto) {
        Cuenta cuenta = buscarCuentaPorId(id);
        validarCuentaActiva(cuenta);

        if (!cuenta.getNumeroCuenta().equals(dto.getNumeroCuenta())
                && cuentaRepository.existsByNumeroCuenta(dto.getNumeroCuenta())) {
            throw new DuplicateResourceException("Ya existe una cuenta con número: " + dto.getNumeroCuenta());
        }

        cuenta.setNumeroCuenta(dto.getNumeroCuenta());
        cuenta.setTipoCuenta(dto.getTipoCuenta());
        cuenta.setEstado(dto.getEstado());
        cuenta.setClienteId(dto.getClienteId());

        Cuenta actualizada = cuentaRepository.save(cuenta);
        return cuentaMapper.toResponseDto(actualizada);
    }

    /**
     * Se aplica Soft Delete.
     * F1 -> requiere CRU en entidad Cuenta.
     * No se aplica Hard Delete, si no solo eliminar = estado -> false.
     */
    @Override
    public void eliminar(Long id) {
        Cuenta cuenta = buscarCuentaPorId(id);
        validarCuentaActiva(cuenta);
        cuenta.setEstado(false);
        cuentaRepository.save(cuenta);
    }

    private Cuenta buscarCuentaPorId(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + id));
    }

    private void validarCuentaActiva(Cuenta cuenta) {
        if (!cuenta.getEstado()) {
            throw new ResourceNotFoundException("Cuenta con id " + cuenta.getId() + " se encuentra inactiva");
        }
    }
}
