# Devsu - Prueba Técnica | Arquitectura de Microservicios

Sistema bancario basado en microservicios para gestión de clientes, cuentas y movimientos financieros.

## Stack Tecnológico

| Tecnología | Versión | Justificación |
|---|---|---|
| Java | 21 LTS | Virtual Threads, Pattern Matching, Records, Sealed Classes |
| Spring Boot | 4.0.3 | Última versión estable (Feb 2026), Jakarta EE 11, Servlet 6.1 |
| PostgreSQL | 16 | ACID compliant, ideal para transacciones financieras |
| RabbitMQ | 3.13 | Comunicación asíncrona entre microservicios |
| Docker | - | Despliegue en contenedores |
| MapStruct | 1.6.3 | Mapeo Entity ↔ DTO en tiempo de compilación |
| Testcontainers | 2.0 | Tests de integración con infraestructura real |

## Arquitectura
![Arquitectura](diagrma-arquitectura.svg)

### Decisiones Arquitectónicas

**Database per Service**
Cada microservicio es dueño de sus datos. Si `ms-cuentas` se cae, `ms-clientes` sigue operando. Si comparten base de datos, no son microservicios reales, son un monolito distribuido.

**Layered Architecture**
Capas limpias (Controller → Service → Repository) con DTOs bien definidos. Hexagonal sería viable, pero para el alcance actual, Layered Architecture cumple con los principios SOLID sin complejidad innecesaria.

**RabbitMQ (Topic Exchange)**
Cuando se crea/actualiza/elimina un Cliente, `ms-clientes` publica un evento. `ms-cuentas` lo consume para mantener una referencia local del cliente (eventual consistency). RabbitMQ sobre Kafka por principio YAGNI: el volumen no justifica Kafka.

**Sin API Gateway**
Para 2 microservicios, un Gateway añade complejidad sin beneficio real. En producción con más servicios, se agregaría Spring Cloud Gateway para routing centralizado, rate limiting y autenticación.

**Identificadores de Negocio**
Los microservicios se comunican por UUID (`clienteId`), nunca por PKs internos. Esto garantiza desacoplamiento total.

## Funcionalidades Implementadas

| Código | Descripción | Estado |
|---|---|---|
| F1 | CRUD Clientes, CRU Cuentas y Movimientos | OK |
| F2 | Registro de movimientos con actualización de saldo | OK |
| F3 | Validación "Saldo no disponible" | OK |
| F4 | Reporte Estado de Cuenta por rango de fechas y cliente | OK |
| F5 | Prueba unitaria para entidad Cliente | OK |
| F6 | Prueba de integración con Testcontainers | OK |
| F7 | Despliegue en contenedores Docker | OK |

## Buenas Prácticas Aplicadas

- **GlobalExceptionHandler** con `@ControllerAdvice`: Manejo centralizado de excepciones con respuestas consistentes `{timestamp, status, error, message, path}`.
- **Bean Validation** (`@Valid`): Validación declarativa con `@NotBlank`, `@NotNull`, `@Min`, `@Max`, `@DecimalMin` en DTOs.
- **MapStruct**: Mapeo Entity ↔ DTO en tiempo de compilación. Zero-cost en runtime, type-safe, sin reflection.
- **Transaccionalidad**: `@Transactional` en operaciones de movimientos. Actualización de saldo y registro son atómicos.
- **Soft Delete**: Los clientes y cuentas se marcan como inactivos, nunca se eliminan físicamente. Crítico para auditoría financiera.
- **Versionado de API**: Endpoints bajo `/api/v1/` con soporte nativo de Spring Boot 4.
- **Enums** para tipos (`Genero`, `TipoCuenta`, `TipoMovimiento`): Restringen valores válidos a nivel de código y base de datos.
- **`BigDecimal`** para montos: Precisión exacta en operaciones financieras, nunca `Double`.

## Rendimiento, Escalabilidad y Resiliencia

### Rendimiento
- `BigDecimal` para dinero (precisión, no `Double`)
- `@Transactional(readOnly = true)` en lecturas (evita dirty-checking)
- `open-in-view: false` (evita queries N+1 silenciosas)
- Denormalización del nombre del cliente (evita llamadas HTTP en cada reporte)
- Índices vía constraints `unique` en campos de búsqueda

### Escalabilidad
- Arquitectura de microservicios (escalan independientemente)
- Database per service (no comparten BD)
- Comunicación asíncrona con RabbitMQ (desacopla los servicios)
- Docker containers (portabilidad y replicabilidad)

### Resiliencia
- `try/catch` en `ClienteRestClient` (si `ms-clientes` está caído, la cuenta se crea sin nombre)
- Queue durable en RabbitMQ (mensajes persisten si el consumer está caído)
- Soft delete (no se pierden datos)
- `GlobalExceptionHandler` (errores controlados, nunca stack traces al cliente)
- Health checks con Actuator
- Retry mechanism con Spring Retry (3 reintentos con backoff exponencial para llamadas síncronas entre servicios)

## Endpoints

### ms-clientes (Puerto 8081)

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/clientes` | Crear cliente |
| GET | `/api/v1/clientes` | Listar todos los clientes |
| GET | `/api/v1/clientes/{id}` | Obtener cliente por ID |
| GET | `/api/v1/clientes/by-cliente-id/{clienteId}` | Obtener cliente por UUID |
| PUT | `/api/v1/clientes/{id}` | Actualizar cliente completo |
| PATCH | `/api/v1/clientes/{id}` | Actualizar cliente parcial |
| DELETE | `/api/v1/clientes/{id}` | Eliminar cliente (soft delete) |

### ms-cuentas (Puerto 8082)

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/cuentas` | Crear cuenta |
| GET | `/api/v1/cuentas` | Listar todas las cuentas |
| GET | `/api/v1/cuentas/{id}` | Obtener cuenta por ID |
| PUT | `/api/v1/cuentas/{id}` | Actualizar cuenta |
| DELETE | `/api/v1/cuentas/{id}` | Eliminar cuenta (soft delete) |
| POST | `/api/v1/movimientos` | Registrar movimiento |
| GET | `/api/v1/movimientos` | Listar movimientos |
| GET | `/api/v1/movimientos/{id}` | Obtener movimiento por ID |
| PUT | `/api/v1/movimientos/{id}` | Actualizar movimiento |
| GET | `/api/v1/reportes` | Reporte estado de cuenta |

**Parámetros del reporte:**
```
GET /api/v1/reportes?clienteId={uuid}&fechaInicio=2026-03-01&fechaFin=2026-03-13
```

## Instrucciones de Despliegue

### Prerrequisitos
- Docker y Docker Compose instalados
- Puertos disponibles: 5436, 5437, 5672, 8081, 8082, 15672

### Opción 1: Docker Compose

```bash
# Clonar el repositorio
git clone https://github.com/FerNicolas117/devsu-prueba-tecnica.git
cd devsu-prueba-tecnica

# Levantar toda la infraestructura + microservicios
docker-compose up -d --build

# Verificar que los 5 contenedores estén corriendo
docker-compose ps
```

Los servicios estarán disponibles en:
- **ms-clientes:** http://localhost:8081
- **ms-cuentas:** http://localhost:8082
- **RabbitMQ Management:** http://localhost:15672 (usuario: `devsu`, contraseña: `devsu2026`)

### Opción 2: Desarrollo local

```bash
# 1. Levantar solo la infraestructura (PostgreSQL + RabbitMQ)
docker-compose up -d postgres-clientes postgres-cuentas rabbitmq

# 2. Ejecutar ms-clientes desde IntelliJ o terminal
cd ms-clientes
./mvnw spring-boot:run

# 3. En otra terminal, ejecutar ms-cuentas
cd ms-cuentas
./mvnw spring-boot:run
```

### Detener los servicios

```bash
docker-compose down       # Detener contenedores (mantiene datos)
docker-compose down -v    # Detener y eliminar volúmenes (datos limpios)
```

## Ejecución de Tests

```bash
# Pruebas unitarias + integración de ms-clientes
cd ms-clientes
./mvnw test

# Pruebas de ms-cuentas
cd ms-cuentas
./mvnw test
```

**Nota:** Las pruebas de integración requieren Docker corriendo, ya que Testcontainers levanta contenedores de PostgreSQL y RabbitMQ automáticamente.

## Estructura del Proyecto

```
devsu/...
```

## Datos de Prueba

Los datos iniciales están disponibles en `BaseDatos.sql`. Alternativamente, se pueden crear mediante los endpoints REST usando la colección de Postman incluida.

### Clientes

| Nombre | Dirección | Teléfono | Contraseña | Estado |
|---|---|---|---|---|
| Jose Lema | Otavalo sn y principal | 098254785 | 1234 | True |
| Marianela Montalvo | Amazonas y NNUU | 097548965 | 5678 | True |
| Juan Osorio | 13 junio y Equinoccial | 098874587 | 1245 | True |

### Cuentas

| Número Cuenta | Tipo | Saldo Inicial | Estado | Cliente |
|---|---|---|---|---|
| 478758 | Ahorros | 2000 | True | Jose Lema |
| 225487 | Corriente | 100 | True | Marianela Montalvo |
| 495878 | Ahorros | 0 | True | Juan Osorio |
| 496825 | Ahorros | 540 | True | Marianela Montalvo |
| 585545 | Corriente | 1000 | True | Jose Lema |

## Autor

Fernando Nicolás — Prueba Técnica para Devsu
