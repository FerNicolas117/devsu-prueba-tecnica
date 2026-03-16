# Devsu - Prueba Técnica/Práctica | Arquitectura de Microservicios

Sistema bancario basado en microservicios para gestión de clientes, cuentas y movimientos financieros.

## Documentación Completa del Proyecto
> ### **Documentación completa:** [Ver en Notion](https://tiny-hat-fd7.notion.site/Devsu-Prueba-T-cnica-Pr-ctica-Arquitectura-de-Microservicios-324307a950668044a330e41f6fd0425e)

## Probar directamente los Endpoints
Importar el archivo /Devsu-Prueba-Tecnica.postman_collection.json a Postman y ejecutar cada uno de los Endpoints.
La colección ya tiene configuradas las variables de cada microservicio.

## Arquitectura
<p align="center">
  <img src="docs/diagrama-arquitectura.svg" alt="Arquitectura" width="600"/>
</p>

### Decisiones Arquitectónicas

**Database per Service**
Cada microservicio es dueño de sus datos. Si `ms-cuentas` se cae, `ms-clientes` sigue operando. Si comparten base de datos, no son microservicios reales, son un monolito distribuido.

**Layered Architecture**
Capas limpias (Controller → Service → Repository) con DTOs bien definidos. Hexagonal sería viable, pero para el alcance actual, Layered Architecture cumple con los principios SOLID sin complejidad innecesaria.

**RabbitMQ (Topic Exchange)**
Cuando se crea/actualiza/elimina un Cliente, `ms-clientes` publica un evento. `ms-cuentas` lo consume para mantener una referencia local del cliente (eventual consistency). RabbitMQ sobre Kafka por principio YAGNI.

**Comunicación síncrona (REST)**
Al crear una cuenta, ms-cuentas consulta a ms-clientes para obtener el nombre del cliente. Implementa retry con backoff exponencial (Spring Retry).

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

## JSON Reporte
```json
{
    "cliente": "Araceli Arreguin Chavez",
    "fechaInicio": "01/03/2026",
    "fechaFin": "16/03/2026",
    "totalMovimientos": 5,
    "resumenGeneral": {
        "totalDepositos": 800.00,
        "totalRetiros": 1200.00,
        "cantidadDepositos": 2,
        "cantidadRetiros": 3
    },
    "cuentas": [
        {
            "numeroCuenta": "888777",
            "tipoCuenta": "CORRIENTE",
            "saldoInicial": 2000.00,
            "saldoDisponible": 1600.00,
            "estado": true,
            "totalMovimientos": 5,
            "resumen": {
                "totalDepositos": 800.00,
                "totalRetiros": 1200.00,
                "cantidadDepositos": 2,
                "cantidadRetiros": 3
            },
            "movimientos": [
                {
                    "fecha": "16/03/2026 08:34:08",
                    "cliente": "Araceli Arreguin Chavez",
                    "numeroCuenta": "888777",
                    "tipo": "CORRIENTE",
                    "saldoInicial": 2000.00,
                    "estado": true,
                    "movimiento": -400.00,
                    "saldoDisponible": 1600.00
                },
                {
                    "fecha": "16/03/2026 08:34:08",
                    "cliente": "Araceli Arreguin Chavez",
                    "numeroCuenta": "888777",
                    "tipo": "CORRIENTE",
                    "saldoInicial": 2000.00,
                    "estado": true,
                    "movimiento": 300.00,
                    "saldoDisponible": 2000.00
                },
                {
                    "fecha": "16/03/2026 08:32:11",
                    "cliente": "Araceli Arreguin Chavez",
                    "numeroCuenta": "888777",
                    "tipo": "CORRIENTE",
                    "saldoInicial": 2000.00,
                    "estado": true,
                    "movimiento": -300.00,
                    "saldoDisponible": 1700.00
                },
                {
                    "fecha": "16/03/2026 08:32:11",
                    "cliente": "Araceli Arreguin Chavez",
                    "numeroCuenta": "888777",
                    "tipo": "CORRIENTE",
                    "saldoInicial": 2000.00,
                    "estado": true,
                    "movimiento": 500.00,
                    "saldoDisponible": 2000.00
                },
                {
                    "fecha": "16/03/2026 08:31:15",
                    "cliente": "Araceli Arreguin Chavez",
                    "numeroCuenta": "888777",
                    "tipo": "CORRIENTE",
                    "saldoInicial": 2000.00,
                    "estado": true,
                    "movimiento": -500.00,
                    "saldoDisponible": 1500.00
                }
            ]
        }
    ]
}
```

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
| PATCH | `/api/v1/movimientos/{id}` | Actualizar movimiento |
| GET | `/api/v1/reportes` | Reporte estado de cuenta |

**Parámetros del reporte:**
```
GET /api/v1/reportes?clienteId={uuid}&fechaInicio=2026-03-01&fechaFin=2026-03-13
```

## Instrucciones de Despliegue

### Prerrequisitos
- Docker y Docker Compose instalados
- Puertos disponibles: 5436, 5437, 5672, 8081, 8082, 15672

### Docker Compose

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

### Desarrollo local

```bash
# 1. Levantar solo la infraestructura (PostgreSQL + RabbitMQ)
docker-compose up -d postgres-clientes postgres-cuentas rabbitmq

# 2. Ejecutar cada microservicio desde IntelliJ o terminal
cd ms-clientes && ./mvnw spring-boot:run
cd ms-cuentas && ./mvnw spring-boot:run
```

### Ejecución de Tests
```bash
cd ms-clientes && ./mvnw test
cd ms-cuentas && ./mvnw test
```
Las pruebas de integración requieren Docker corriendo (Testcontainers).

### Detener los servicios

```bash
docker-compose down       # Detener contenedores (mantiene datos)
docker-compose down -v    # Detener y eliminar volúmenes (datos limpios)
```

## Despliegue en AWS
La aplicación se encuentra desplegada en una instancia EC2 con CI/CD automático vía GitHub Actions:
| Servicio | URL |
|---|---|
| Swagger ms-clientes | http://18.117.236.189:8081/swagger-ui.html |
| Swagger ms-cuentas | http://18.117.236.189:8082/swagger-ui.html |
| Health ms-clientes | http://18.117.236.189:8081/actuator/health |
| Health ms-cuentas | http://18.117.236.189:8082/actuator/health |

## Autor

Fernando Nicolás — Prueba Técnica/Práctica para Devsu
