package com.devsu.mscuentas.entity;

import com.devsu.mscuentas.entity.enums.TipoCuenta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cuentas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 20)
    private String numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 10)
    private TipoCuenta tipoCuenta;

    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial;

    @Column(name = "saldo_disponible", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoDisponible;

    @Column(nullable = false)
    private Boolean estado;

    // Referencia al cliente en ms-clientes, no es una FK real, es comunicación entre microservicios.
    @Column(name = "cliente_id", nullable = false, length = 20)
    private String clienteId;

    // Nombre del cliente (copia local para reportes, se sincroniza vía RabbitMQ).
    @Column(name = "cliente_nombre", length = 100)
    private String clienteNombre;

    @OneToMany(mappedBy = "cuenta", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Movimiento> movimientos = new ArrayList<>();
}
