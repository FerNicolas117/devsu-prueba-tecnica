package com.devsu.mscuentas.service.impl;

import com.devsu.mscuentas.dto.MovimientoRequestDto;
import com.devsu.mscuentas.dto.MovimientoResponseDto;
import com.devsu.mscuentas.dto.ReporteMovimientoDto;
import com.devsu.mscuentas.dto.ReporteResponseDto;
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
     * Actualiza un movimiento existente.
     * Nota: En un sistema financiero real, los movimientos serían inmutables y se manejarían
     * con contra-asietons. Se implementa por requerimiento de la prueba técnica (F1 -> CRU para Movimiento).
     */
    @Override
    public MovimientoResponseDto actualizar(Long id, MovimientoRequestDto dto) {
        Movimiento movimiento = buscarMovimientoPorId(id);
        Cuenta cuenta = movimiento.getCuenta();
        validarCuentaActiva(cuenta);

        // Revertir el movimiento anterior.
        BigDecimal saldoRevertido = cuenta.getSaldoDisponible().subtract(movimiento.getValor());

        // Aplicar el nuevo valor.
        BigDecimal nuevoValor = dto.getValor();
        BigDecimal nuevoSaldo = saldoRevertido.add(nuevoValor);

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException();
        }

        TipoMovimiento tipo = nuevoValor.compareTo(BigDecimal.ZERO) >= 0
                ? TipoMovimiento.DEPOSITO
                : TipoMovimiento.RETIRO;

        movimiento.setValor(nuevoValor);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setSaldo(nuevoSaldo);
        cuenta.setSaldoDisponible(nuevoSaldo);

        cuentaRepository.save(cuenta);
        Movimiento actualizado = movimientoRepository.save(movimiento);
        return movimientoMapper.toResponseDto(actualizado);
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
