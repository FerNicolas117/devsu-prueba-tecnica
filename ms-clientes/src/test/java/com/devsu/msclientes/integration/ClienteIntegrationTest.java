package com.devsu.msclientes.integration;

import com.devsu.msclientes.dto.ClienteRequestDto;
import com.devsu.msclientes.entity.enums.Genero;
import com.devsu.msclientes.repository.ClienteRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Cliente API - Pruebas de Integración")
public class ClienteIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("db_clientes_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String clienteIdCreado;

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/clientes - Debe crear un cliente y retornar 201")
    void crearCliente() throws Exception {
        ClienteRequestDto request = new ClienteRequestDto();
        request.setNombre("Jose Lema");
        request.setGenero(Genero.MASCULINO);
        request.setEdad(30);
        request.setIdentificacion("1234567890");
        request.setDireccion("Otavalo sn y principal");
        request.setTelefono("098254785");
        request.setContrasena("1234");
        request.setEstado(true);

        MvcResult result = mockMvc.perform(post("/api/v1/clientes")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.identificacion").value("1234567890"))
                .andExpect(jsonPath("$.estado").value(true))
                .andExpect(jsonPath("$.clienteId").isNotEmpty())
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andReturn();

        // Guardar el Id para los siguientes tests.
        String responseBody = result.getResponse().getContentAsString();
        clienteIdCreado = objectMapper.readTree(responseBody).get("id").asText();
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/clientes - Debe rechazar identificación duplicada con 409")
    void crearCliente_duplicado() throws Exception {
        ClienteRequestDto request = new ClienteRequestDto();
        request.setNombre("Otro Cliente");
        request.setGenero(Genero.FEMENINO);
        request.setEdad(25);
        request.setIdentificacion("1234567890"); // Misma identificación.
        request.setDireccion("Otra dirección");
        request.setTelefono("099999999");
        request.setContrasena("5678");
        request.setEstado(true);

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Ya existe un cliente")));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/clientes - Debe rechazar request inválido con 400")
    void crearCliente_validacion() throws Exception {
        // Request sin nombre ni identificación
        String requestInvalido = """
                    {
                        "genero": "MASCULINO",
                        "edad": 30
                    }
                    """;

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(requestInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isNotEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/clientes - Debe listar todos los clientes")
    void obtenerTodos() throws Exception {
        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].nombre").value("Jose Lema"));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/clientes/{id} - Debe obtener un cliente por ID")
    void obtenerPorId() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/" + clienteIdCreado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.identificacion").value("1234567890"));
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/v1/clientes/99 - Debe retornar 404 si no existe")
    void obtenerPorId_noExiste() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Cliente no encontrado")));
    }

    @Test
    @Order(7)
    @DisplayName("PUT /api/v1/clientes/{id} - Debe actualizar un cliente")
    void actualizarCliente() throws Exception {
        ClienteRequestDto request = new ClienteRequestDto();
        request.setNombre("Jose Lema Actualizado");
        request.setGenero(Genero.MASCULINO);
        request.setEdad(31);
        request.setIdentificacion("1234567890");
        request.setDireccion("Nueva dirección");
        request.setTelefono("099999999");
        request.setContrasena("4321");
        request.setEstado(true);

        mockMvc.perform(put("/api/v1/clientes/" + clienteIdCreado)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Jose Lema Actualizado"))
                .andExpect(jsonPath("$.edad").value(31));
    }

    @Test
    @Order(8)
    @DisplayName("PATCH /api/v1/clientes/{id} - Debe actualizar parcialmente")
    void actualizarParcial() throws Exception {
        String patchBody = """
                {
                    "telefono": "088888888"
                }
                """;

        mockMvc.perform(patch("/api/v1/clientes/" + clienteIdCreado)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(patchBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefono").value("088888888"))
                .andExpect(jsonPath("$.nombre").value("Jose Lema Actualizado")); // No se modificó
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /api/v1/clientes/{id} - Debe hacer soft delete")
    void eliminarCliente() throws Exception {
        mockMvc.perform(delete("/api/v1/clientes/" + clienteIdCreado))
                .andExpect(status().isNoContent());

        // Verificar que sigue existiendo pero con estado false
        mockMvc.perform(get("/api/v1/clientes/" + clienteIdCreado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(false));
    }

    @Test
    @Order(10)
    @DisplayName("POST /api/v1/clientes - Debe rechazar género inválido con 400")
    void crearCliente_generoInvalido() throws Exception {
        String requestGeneroInvalido = """
                {
                    "nombre": "Test",
                    "genero": "INVALIDO",
                    "edad": 25,
                    "identificacion": "9999999999",
                    "direccion": "Test",
                    "telefono": "099999999",
                    "contrasena": "1234",
                    "estado": true
                }
                """;

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(requestGeneroInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("solo acepta los valores")));
    }
}
