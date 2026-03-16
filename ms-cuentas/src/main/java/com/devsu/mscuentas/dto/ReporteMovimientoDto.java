package com.devsu.mscuentas.dto;

import com.devsu.mscuentas.entity.enums.TipoCuenta;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteMovimientoDto {

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime fecha;

    private String tipoMovimiento;
    private BigDecimal movimiento;
    private BigDecimal saldoDisponible;

    /*
    private String cliente;
    private String numeroCuenta;
    private TipoCuenta tipo;
    private BigDecimal saldoInicial;
    private Boolean estado;
     */
}
