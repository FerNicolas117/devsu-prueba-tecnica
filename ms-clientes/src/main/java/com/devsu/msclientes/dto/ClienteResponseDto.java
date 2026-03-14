package com.devsu.msclientes.dto;

import com.devsu.msclientes.entity.enums.Genero;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponseDto {

    private Long id;
    private String clienteId;
    private String nombre;
    private Genero genero; // Jackson convierte el Enum a String.
    private Integer edad;
    private String identificacion;
    private String direccion;
    private String telefono;
    private Boolean estado;

    // No exponemos la contraseña en la respuesta.
}
