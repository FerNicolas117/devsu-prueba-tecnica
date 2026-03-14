package com.devsu.msclientes.event;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteEvent implements Serializable {

    private String clienteId;
    private String nombre;
    private String accion; // CREADO, ACTUALIZADO, ELIMINADO
}
