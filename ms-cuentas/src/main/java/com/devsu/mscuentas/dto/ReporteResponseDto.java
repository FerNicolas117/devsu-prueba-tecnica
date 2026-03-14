package com.devsu.mscuentas.dto;

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

    // Resumen.
    private ResumenDto resumen;

    // Detalles.
    private List<ReporteMovimientoDto> movimientos;

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
}
