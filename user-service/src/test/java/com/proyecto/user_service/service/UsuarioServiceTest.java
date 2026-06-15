package com.proyecto.user_service.service;

import com.proyecto.user_service.enums.Rol;
import com.proyecto.user_service.exception.ResourceNotFoundException;
import com.proyecto.user_service.model.Usuario;
import com.proyecto.user_service.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private Usuario.UsuarioBuilder usuarioBuilder;

    @BeforeEach
    void setUp() {
        usuarioBuilder = Usuario.builder()
                .id(1L)
                .rut("21719226-9")
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan@colegioohiggins.cl")
                .password("Pass123!")
                .rol(Rol.DOCENTE)
                .enabled(true);
    }

    // Listar Usuarios

    @Test
    void listarUsuarios_returnsAllUsers() {
        var usuario1 = usuarioBuilder.build();
        var usuario2 = usuarioBuilder.id(2L).email("otro@colegioohiggins.cl").build();
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario1, usuario2));

        var result = usuarioService.listarUsuarios();

        assertThat(result).hasSize(2);
    }

    // Encontrar Usuario por ID

    @Test
    void findById_returnsUser_whenFound() {
        var usuario = usuarioBuilder.build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        var result = usuarioService.findById(1L);

        assertThat(result).isEqualTo(usuario);
    }

    @Test
    void findById_throws_whenNotFound() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuario no encontrado con id: 99");
    }

    // Encontrar usuario por Email

    @Test
    void findByUsername_returnsUser_whenFound() {
        var usuario = usuarioBuilder.build();
        when(usuarioRepository.findByEmail("juan@colegioohiggins.cl")).thenReturn(Optional.of(usuario));

        var result = usuarioService.findByUsername("juan@colegioohiggins.cl");

        assertThat(result).isEqualTo(usuario);
    }

    @Test
    void findByUsername_throws_whenNotFound() {
        when(usuarioRepository.findByEmail("unknown@colegioohiggins.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findByUsername("unknown@colegioohiggins.cl"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuario no encontrado con email: unknown@colegioohiggins.cl");
    }

    // Encontrar usuario por RUT


    @Test
    void findByRut_returnsUser_whenFound() {
        var usuario = usuarioBuilder.build();
        when(usuarioRepository.findByRut("21719226-9")).thenReturn(Optional.of(usuario));

        var result = usuarioService.findByRut("21.719.226-9");

        assertThat(result).isEqualTo(usuario);
        verify(usuarioRepository).findByRut("21719226-9");
    }

    @Test
    void findByRut_throws_whenNotFound() {
        when(usuarioRepository.findByRut("21719226-9")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findByRut("21.719.226-9"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuario no encontrado con RUT: 21.719.226-9");
    }

    // Metodo Existe por ID

    @Test
    void existsById_returnsTrue() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        assertThat(usuarioService.existsById(1L)).isTrue();
    }

    @Test
    void existsById_returnsFalse() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThat(usuarioService.existsById(99L)).isFalse();
    }

    // Guardar Usuario

    @Test
    void save_encodesPasswordAndSaves() {
        var usuario = usuarioBuilder.build();
        when(passwordEncoder.encode(any())).thenReturn("encoded-pass");
        when(usuarioRepository.save(any())).thenReturn(usuario);

        var result = usuarioService.save(usuario);

        verify(passwordEncoder).encode("Pass123!");
        verify(usuarioRepository).save(usuario);
        assertThat(result).isEqualTo(usuario);
    }

    // Actualizar Usuario

    @Test
    void update_updatesOnlyNonNullAndNonEmptyFields() {
        var existing = usuarioBuilder
                .nombres("Original").apellidos("Original")
                .password("old-pass").enabled(true)
                .build();

        var details = Usuario.builder()
                .nombres("Nuevo")
                .apellidos("")
                .password("NewPass123!")
                .enabled(null)
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("NewPass123!")).thenReturn("encoded-new");

        usuarioService.update(1L, details);

        assertThat(existing.getNombres()).isEqualTo("Nuevo");
        assertThat(existing.getApellidos()).isEqualTo("Original");
        assertThat(existing.getPassword()).isEqualTo("encoded-new");
        assertThat(existing.getEnabled()).isTrue();
    }

    @Test
    void update_throws_whenUserNotFound() {
        var details = Usuario.builder().nombres("Nuevo").build();
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.update(99L, details))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuario no encontrado con id: 99");
    }

    // Borrar Usuario (Deshabilitar cuenta)

    @Test
    void deleteById_setsEnabledFalse() {
        var usuario = usuarioBuilder.enabled(true).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.deleteById(1L);

        assertThat(usuario.getEnabled()).isFalse();
        verify(usuarioRepository).save(usuario);
    }
}
