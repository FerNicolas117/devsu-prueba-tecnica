-- BaseDatos.sql
-- Hibernate genera las tablas automáticamente con ddl-auto: update, este script es de referencia.

-- Base de datos: db_clientes
CREATE TABLE IF NOT EXISTS personas (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    genero          VARCHAR(15) NOT NULL,
    edad            INTEGER NOT NULL,
    identificacion  VARCHAR(20) NOT NULL UNIQUE,
    direccion       VARCHAR(200) NOT NULL,
    telefono        VARCHAR(15) NOT NULL
);

CREATE TABLE IF NOT EXISTS clientes (
    id              BIGINT PRIMARY KEY REFERENCES personas(id),
    cliente_id      VARCHAR(20) NOT NULL UNIQUE,
    contrasena      VARCHAR(255) NOT NULL,
    estado          BOOLEAN NOT NULL
);

-- Base de datos: db_cuentas
CREATE TABLE IF NOT EXISTS cuentas (
    id                  BIGSERIAL PRIMARY KEY,
    numero_cuenta       VARCHAR(20) NOT NULL UNIQUE,
    tipo_cuenta         VARCHAR(15) NOT NULL,
    saldo_inicial       NUMERIC(15,2) NOT NULL,
    saldo_disponible    NUMERIC(15,2) NOT NULL,
    estado              BOOLEAN NOT NULL,
    cliente_id          VARCHAR(20) NOT NULL,
    cliente_nombre      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS movimientos (
    id                  BIGSERIAL PRIMARY KEY,
    fecha               TIMESTAMP NOT NULL,
    tipo_movimiento     VARCHAR(15) NOT NULL,
    valor               NUMERIC(15,2) NOT NULL,
    saldo               NUMERIC(15,2) NOT NULL,
    cuenta_id           BIGINT NOT NULL REFERENCES cuentas(id)
);

-- Datos iniciales de prueba

-- Clientes (ejecutar en db_clientes)
-- cliente_id se genera automáticamente por la aplicación.
INSERT INTO personas (nombre, genero, edad, identificacion, direccion, telefono)
VALUES
    ('Jose Lema', 'MASCULINO', 30, '1234567890', 'Otavalo sn y principal', '098254785'),
    ('Marianela Montalvo', 'FEMENINO', 28, '0987654321', 'Amazonas y NNUU', '097548965'),
    ('Juan Osorio', 'MASCULINO', 35, '1122334455', '13 junio y Equinoccial', '098874587');

INSERT INTO clientes (id, cliente_id, contrasena, estado)
VALUES
    (1, 'CLI00001', '1234', true),
    (2, 'CLI00002', '5678', true),
    (3, 'CLI00003', '1245', true);

-- Cuentas (ejecutar en db_cuentas)
INSERT INTO cuentas (numero_cuenta, tipo_cuenta, saldo_inicial, saldo_disponible, estado, cliente_id, cliente_nombre)
VALUES
    ('478758', 'AHORROS', 2000, 2000, true, 'CLI00001', 'Jose Lema'),
    ('225487', 'CORRIENTE', 100, 100, true, 'CLI00002', 'Marianela Montalvo'),
    ('495878', 'AHORROS', 0, 0, true, 'CLI00003', 'Juan Osorio'),
    ('496825', 'AHORROS', 540, 540, true, 'CLI00002', 'Marianela Montalvo'),
    ('585545', 'CORRIENTE', 1000, 1000, true, 'CLI00001', 'Jose Lema');