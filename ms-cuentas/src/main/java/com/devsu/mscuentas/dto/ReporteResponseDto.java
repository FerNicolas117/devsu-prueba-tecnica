package com.devsu.mscuentas.dto;

import com.devsu.mscuentas.entity.enums.TipoCuenta;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteResponseDto {

    // Metadata.
    private String cliente;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate fechaInicio;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate fechaFin;

    private int totalMovimientos;

    private ResumenDto resumenGeneral;

    private List<CuentaDetalleDto> cuentas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResumenDto {
        private BigDecimal totalDepositos;
        private BigDecimal totalRetiros;
        private int cantidadDepositos;
        private int cantidadRetiros;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CuentaDetalleDto {
        private String numeroCuenta;
        private TipoCuenta tipoCuenta;
        private BigDecimal saldoInicial;
        private BigDecimal saldoDisponible;
        private Boolean estado;
        private int totalMovimientos;
        private ResumenDto resumen;
        private List<ReporteMovimientoDto> movimientos;
    }
}
