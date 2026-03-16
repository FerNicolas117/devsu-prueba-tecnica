package com.devsu.mscuentas.service.impl;

import com.devsu.mscuentas.dto.*;
import com.devsu.mscuentas.entity.Cuenta;
import com.devsu.mscuentas.entity.Movimiento;
import com.devsu.mscuentas.entity.enums.TipoMovimiento;
import com.devsu.mscuentas.exception.InsufficientBalanceException;
import com.devsu.mscuentas.exception.InvalidDateRangeException;
import com.devsu.mscuentas.exception.ResourceNotFoundException;
import com.devsu.mscuentas.mapper.MovimientoMapper;
import com.devsu.mscuentas.repository.CuentaRepository;
import com.devsu.mscuentas.repository.MovimientoRepository;
import com.devsu.mscuentas.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoMapper movimientoMapper;

    private static final long MAX_RANGO_DIAS = 365;

    @Override
    public MovimientoResponseDto registrar(MovimientoRequestDto dto) {

        // 1. Buscar la cuenta por número.
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(dto.getNumeroCuenta())
                .orElseThrow(() -> new
                        ResourceNotFoundException("Cuenta no encontrada con número: " + dto.getNumeroCuenta()));

        validarCuentaActiva(cuenta);

        // 2. Determinar tipo de movimiento por el signo del valor + -
        BigDecimal valor = dto.getValor();
        TipoMovimiento tipo = valor.compareTo(BigDecimal.ZERO) >= 0
                ? TipoMovimiento.DEPOSITO
                : TipoMovimiento.RETIRO;

        // 3. Calcular nuevo saldo.
        BigDecimal saldoActual = cuenta.getSaldoDisponible();
        BigDecimal nuevoSaldo = saldoActual.add(valor);

        // 4. Punto F3: Validar que no quede saldo negativo.
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException();
        }

        // 5. Crear el movimiento.
        Movimiento movimiento = Movimiento.builder()
                .fecha(LocalDateTime.now())
                .tipoMovimiento(tipo)
                .valor(valor)
                .saldo(nuevoSaldo)
                .cuenta(cuenta)
                .build();

        // 6. Actualizar saldo disponible en la cuenta.
        cuenta.setSaldoDisponible(nuevoSaldo);
        cuentaRepository.save(cuenta);

        // 7. Guardar el movimiento.
        Movimiento guardado = movimientoRepository.save(movimiento);
        return movimientoMapper.toResponseDto(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoResponseDto obtenerPorId(Long id) {
        Movimiento movimiento = buscarMovimientoPorId(id);
        return movimientoMapper.toResponseDto(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponseDto> obtenerTodos() {
        return movimientoMapper.toResponseDtoList(movimientoRepository.findAll());
    }

    /**
     * Corrige un movimiento mediante contra-asiento.
     * No modifica el original -> crea una reversión y un nuevo movimiento.
     */
    @Override
    public MovimientoResponseDto actualizar(Long id, MovimientoUpdateDto dto) {
        Movimiento movimientoOriginal = buscarMovimientoPorId(id);
        Cuenta cuenta = movimientoOriginal.getCuenta();
        validarCuentaActiva(cuenta);

        // 1. Reversión: valor opuesto al original.
        BigDecimal valorReversion = movimientoOriginal.getValor().negate();
        BigDecimal saldoTrasReversion = cuenta.getSaldoDisponible().add(valorReversion);

        TipoMovimiento tipoReversion = valorReversion.compareTo(BigDecimal.ZERO) >= 0
                ? TipoMovimiento.DEPOSITO
                : TipoMovimiento.RETIRO;

        Movimiento reversion = Movimiento.builder()
                .fecha(LocalDateTime.now())
                .tipoMovimiento(tipoReversion)
                .valor(valorReversion)
                .saldo(saldoTrasReversion)
                .cuenta(cuenta)
                .build();

        cuenta.setSaldoDisponible(saldoTrasReversion);
        movimientoRepository.save(reversion);

        // 2. Nuevo movimiento con el valor correcto.
        BigDecimal nuevoValor = dto.getValor();
        BigDecimal nuevoSaldo = saldoTrasReversion.add(nuevoValor);

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException();
        }

        TipoMovimiento tipoNuevo = nuevoValor.compareTo(BigDecimal.ZERO) >= 0
                ? TipoMovimiento.DEPOSITO
                : TipoMovimiento.RETIRO;

        Movimiento nuevoMovimiento = Movimiento.builder()
                .fecha(LocalDateTime.now())
                .tipoMovimiento(tipoNuevo)
                .valor(nuevoValor)
                .saldo(nuevoSaldo)
                .cuenta(cuenta)
                .build();

        cuenta.setSaldoDisponible(nuevoSaldo);
        cuentaRepository.save(cuenta);
        Movimiento guardado = movimientoRepository.save(nuevoMovimiento);

        return movimientoMapper.toResponseDto(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResponseDto generarReporte(String clienteId, LocalDate fechaInicio, LocalDate fechaFin) {

        // Validación, fechaInicio no puede ser posterior a fechaFin.
        if (fechaInicio.isAfter(fechaFin))  {
            throw new InvalidDateRangeException("La fecha de inicio no puede ser posterior a la fecha fin");
        }

        // Validación, rango máximo de 1 año.
        if (ChronoUnit.DAYS.between(fechaInicio, fechaFin) > MAX_RANGO_DIAS) {
            throw new InvalidDateRangeException("El rango de fechas no puede exceder " + MAX_RANGO_DIAS + " días");
        }

        // Buscar todas las cuentas del cliente.
        List<Cuenta> cuentas = cuentaRepository.findByClienteId(clienteId);
        if (cuentas.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron cuentas para el cliente con id: " + clienteId);
        }

        // Convertir fechas a LocalDateTime (inicio y fin del día).
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        List<ReporteMovimientoDto> movimientosReporte = new ArrayList<>();
        String nombreCliente = null;

        for (Cuenta cuenta : cuentas) {
            if (nombreCliente == null) {
                nombreCliente = cuenta.getClienteNombre();
            }

            List<Movimiento> movimientos = movimientoRepository
                    .findByCuentaIdAndFechaBetweenOrderByFechaDesc(cuenta.getId(), inicio, fin);

            for (Movimiento movimiento : movimientos) {
                ReporteMovimientoDto item = ReporteMovimientoDto.builder()
                        .fecha(movimiento.getFecha())
                        .cliente(cuenta.getClienteNombre())
                        .numeroCuenta(cuenta.getNumeroCuenta())
                        .tipo(cuenta.getTipoCuenta())
                        .saldoInicial(cuenta.getSaldoInicial())
                        .estado(cuenta.getEstado())
                        .movimiento(movimiento.getValor())
                        .saldoDisponible(movimiento.getSaldo())
                        .build();

                movimientosReporte.add(item);
            }
        }

        // Calcular resumen.
        BigDecimal totalDepositos = BigDecimal.ZERO;
        BigDecimal totalRetiros = BigDecimal.ZERO;
        int cantidadDepositos = 0;
        int cantidadRetiros = 0;

        for (ReporteMovimientoDto movimiento : movimientosReporte) {
            if (movimiento.getMovimiento().compareTo(BigDecimal.ZERO) >= 0) {
                totalDepositos = totalDepositos.add(movimiento.getMovimiento());
                cantidadDepositos++;
            } else {
                totalRetiros = totalRetiros.add(movimiento.getMovimiento().abs());
                cantidadRetiros++;
            }
        }

        ReporteResponseDto.ResumenDto resumen = ReporteResponseDto.ResumenDto.builder()
                .totalDepositos(totalDepositos)
                .totalRetiros(totalRetiros)
                .cantidadDepositos(cantidadDepositos)
                .cantidadRetiros(cantidadRetiros)
                .build();

        return ReporteResponseDto.builder()
                .cliente(nombreCliente)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalMovimientos(movimientosReporte.size())
                .resumen(resumen)
                .movimientos(movimientosReporte)
                .build();
    }

    private Movimiento buscarMovimientoPorId(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id: " + id));
    }

    private void validarCuentaActiva(Cuenta cuenta) {
        if (!cuenta.getEstado()) {
            throw new ResourceNotFoundException("Cuenta " + cuenta.getNumeroCuenta() + " se encuentra inactiva");
        }
    }
}
