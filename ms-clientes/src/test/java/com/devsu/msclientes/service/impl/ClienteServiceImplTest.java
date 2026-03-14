package com.devsu.msclientes.service.impl;

import com.devsu.msclientes.dto.ClienteRequestDto;
import com.devsu.msclientes.dto.ClienteResponseDto;
import com.devsu.msclientes.entity.Cliente;
import com.devsu.msclientes.entity.enums.Genero;
import com.devsu.msclientes.event.ClienteEventPublisher;
import com.devsu.msclientes.exception.DuplicateResourceException;
import com.devsu.msclientes.exception.ResourceNotFoundException;
import com.devsu.msclientes.mapper.ClienteMapper;
import com.devsu.msclientes.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService - Pruebas Unitarias")
public class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private ClienteEventPublisher eventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteRequestDto requestDto;
    private Cliente cliente;
    private ClienteResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new ClienteRequestDto();
        requestDto.setNombre("Jose Lema");
        requestDto.setGenero(Genero.MASCULINO);
        requestDto.setEdad(30);
        requestDto.setIdentificacion("1234567890");
        requestDto.setDireccion("Otavalo sn y principal");
        requestDto.setTelefono("098254785");
        requestDto.setContrasena("1234");
        requestDto.setEstado(true);

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setClienteId("A1B2C3D4");
        cliente.setNombre("Jose Lema");
        cliente.setGenero(Genero.MASCULINO);
        cliente.setEdad(30);
        cliente.setIdentificacion("1234567890");
        cliente.setDireccion("Otavalo sn y principal");
        cliente.setTelefono("098254785");
        cliente.setContrasena("1234");
        cliente.setEstado(true);

        responseDto = new ClienteResponseDto();
        responseDto.setId(1L);
        responseDto.setClienteId("A1B2C3D4");
        responseDto.setNombre("Jose Lema");
        responseDto.setGenero(Genero.MASCULINO);
        responseDto.setEdad(30);
        responseDto.setIdentificacion("1234567890");
        responseDto.setDireccion("Otavalo sn y principal");
        responseDto.setTelefono("098254785");
        responseDto.setEstado(true);
    }

    @Nested
    @DisplayName("Crear Cliente")
    class CrearCliente {

        @Test
        @DisplayName("Debe crear un cliente exitosamente.")
        void crearCliente_exitoso() {
            // Arrange (preparar).
            when(clienteRepository.existsByIdentificacion(anyString())).thenReturn(false);
            when(clienteMapper.toEntity(any(ClienteRequestDto.class))).thenReturn(cliente);
            when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
            when(clienteMapper.toResponseDto(any(Cliente.class))).thenReturn(responseDto);
            doNothing().when(eventPublisher).publicarEvento(any());
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");

            // Act (ejecutar).
            ClienteResponseDto resultado = clienteService.crear(requestDto);

            // Assert (verificar).
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNombre()).isEqualTo("Jose Lema");
            assertThat(resultado.getIdentificacion()).isEqualTo("1234567890");
            assertThat(resultado.getEstado()).isTrue();

            verify(clienteRepository).existsByIdentificacion("1234567890");
            verify(clienteRepository).save(any(Cliente.class));
            verify(eventPublisher).publicarEvento(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si la identificación ya existe")
        void crearCliente_identificacionDuplicada() {
            // Arrange.
            when(clienteRepository.existsByIdentificacion("1234567890")).thenReturn(true);

            // Act & Assert.
            assertThatThrownBy(() -> clienteService.crear(requestDto))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Ya existe un cliente con identificación");

            verify(clienteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Obtener cliente")
    class ObtenerCliente {

        @Test
        @DisplayName("Debe obtener un cliente por ID")
        void obtenerPorId_exitoso() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteMapper.toResponseDto(cliente)).thenReturn(responseDto);

            ClienteResponseDto resultado = clienteService.obtenerPorId(1L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getNombre()).isEqualTo("Jose Lema");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el cliente no existe")
        void obtenerPorId_noExiste() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clienteService.obtenerPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cliente no encontrado");
        }

        @Test
        @DisplayName("Debe obtener todos los clientes")
        void obtenerTodos_exitoso() {
            when(clienteRepository.findAll()).thenReturn(List.of(cliente));
            when(clienteMapper.toResponseDtoList(anyList())).thenReturn(List.of(responseDto));

            List<ClienteResponseDto> resultado = clienteService.obtenerTodos();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNombre()).isEqualTo("Jose Lema");
        }
    }

    @Nested
    @DisplayName("Actualizar cliente")
    class ActualizarCliente {

        @Test
        @DisplayName("Debe actualizar un cliente exitosamente")
        void actualizar_exitoso() {
            ClienteRequestDto updateDto = new ClienteRequestDto();
            updateDto.setNombre("Jose Lema Actualizado");
            updateDto.setGenero(Genero.MASCULINO);
            updateDto.setEdad(31);
            updateDto.setIdentificacion("1234567890");
            updateDto.setDireccion("Nueva dirección");
            updateDto.setTelefono("099999999");
            updateDto.setContrasena("4321");
            updateDto.setEstado(true);

            Cliente clienteActualizado = new Cliente();
            clienteActualizado.setId(1L);
            clienteActualizado.setClienteId("A1B2C3D4");
            clienteActualizado.setNombre("Jose Lema Actualizado");
            clienteActualizado.setEstado(true);

            ClienteResponseDto responseDtoActualizado = new ClienteResponseDto();
            responseDtoActualizado.setId(1L);
            responseDtoActualizado.setNombre("Jose Lema Actualizado");

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            // when(clienteRepository.existsByIdentificacion("1234567890")).thenReturn(true);
            doNothing().when(clienteMapper).updateEntityFromDto(any(), any());
            when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);
            when(clienteMapper.toResponseDto(any(Cliente.class))).thenReturn(responseDtoActualizado);
            doNothing().when(eventPublisher).publicarEvento(any());
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");

            ClienteResponseDto resultado = clienteService.actualizar(1L, updateDto);

            assertThat(resultado.getNombre()).isEqualTo("Jose Lema Actualizado");
            verify(clienteRepository).save(any(Cliente.class));
            verify(eventPublisher).publicarEvento(any());
        }
    }

    @Nested
    @DisplayName("Eliminar cliente (soft delete)")
    class EliminarCliente {

        @Test
        @DisplayName("Debe marcar el cliente como inactivo")
        void eliminar_softDelete() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
            doNothing().when(eventPublisher).publicarEvento(any());

            clienteService.eliminar(1L);

            assertThat(cliente.getEstado()).isFalse();
            verify(clienteRepository).save(cliente);
            verify(clienteRepository, never()).delete(any());
            verify(eventPublisher).publicarEvento(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción si el cliente no existe")
        void eliminar_noExiste() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clienteService.eliminar(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
