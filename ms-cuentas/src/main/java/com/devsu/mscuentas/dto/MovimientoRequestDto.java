package com.devsu.mscuentas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoRequestDto {

    @NotBlank(message = "El número de cuenta es obligatorio")
    private String numeroCuenta;

    @NotNull(message = "El valor es obligatorio")
    private BigDecimal valor;

    // Valor positivo = depósito, valor negativo = retiro
    // Para no meter un if, el tipo de movimiento se deduce por el signo + -
}
