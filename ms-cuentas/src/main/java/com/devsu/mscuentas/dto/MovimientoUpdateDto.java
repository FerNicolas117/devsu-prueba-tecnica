package com.devsu.mscuentas.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoUpdateDto {

    @NotNull(message = "El valor es obligatorio")
    private BigDecimal valor;
}
