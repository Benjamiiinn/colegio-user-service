package com.proyecto.user_service.service.impl;

import com.proyecto.user_service.enums.Rol;
import com.proyecto.user_service.exception.BusinessRuleException;
import com.proyecto.user_service.exception.ResourceNotFoundException;
import com.proyecto.user_service.model.RefreshToken;
import com.proyecto.user_service.model.Usuario;
import com.proyecto.user_service.repository.UsuarioRepository;
import com.proyecto.user_service.request.AuthenticationRequest;
import com.proyecto.user_service.request.RegisterRequest;
import com.proyecto.user_service.response.AuthResponse;
import com.proyecto.user_service.service.JwtService;
import com.proyecto.user_service.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @InjectMocks
    private AuthenticationServiceImpl authService;

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RefreshTokenService refreshTokenService;

    private RegisterRequest.RegisterRequestBuilder requestBuilder;
    private Usuario.UsuarioBuilder usuarioBuilder;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        requestBuilder = RegisterRequest.builder()
                .rut("21719226-9")
                .nombres("Benjamin")
                .apellidos("Gonzalez")
                .password("Pass123!");

        usuarioBuilder = Usuario.builder()
                .id(1L)
                .rut("21719226-9")
                .nombres("Benjamin")
                .apellidos("Gonzalez")
                .password("encoded-pass")
                .enabled(true);

        refreshToken = RefreshToken.builder().token("mock-refresh").build();
    }

    // Registro — Exitoso

    @Test
    void register_success_admin() {
        var request = requestBuilder.email("admin@colegioohiggins.cl").rol(Rol.ADMIN).build();

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByRut(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-pass");
        when(jwtService.generateToken(any())).thenReturn("mock-jwt");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("mock-jwt");
        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh");
        assertThat(response.getEmail()).isEqualTo("admin@colegioohiggins.cl");
        assertThat(response.getTokenType()).isEqualTo("BEARER");
        assertThat(response.getRoles()).contains("ROLE_ADMIN");
    }

    @Test
    void register_success_docente() {
        var request = requestBuilder.email("docente@colegioohiggins.cl").rol(Rol.DOCENTE).build();

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByRut(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-pass");
        when(jwtService.generateToken(any())).thenReturn("mock-jwt");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);

        AuthResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("docente@colegioohiggins.cl");
        assertThat(response.getRoles()).contains("ROLE_DOCENTE");
    }

    @Test
    void register_success_estudiante() {
        var request = requestBuilder.email("alumno@alumnos.colegioohiggins.cl").rol(Rol.ESTUDIANTE).build();

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByRut(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-pass");
        when(jwtService.generateToken(any())).thenReturn("mock-jwt");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);

        AuthResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("alumno@alumnos.colegioohiggins.cl");
        assertThat(response.getRoles()).contains("ROLE_ESTUDIANTE");
    }

    @Test
    void register_success_apoderado() {
        var request = requestBuilder.email("apoderado@gmail.com").rol(Rol.APODERADO).build();

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByRut(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-pass");
        when(jwtService.generateToken(any())).thenReturn("mock-jwt");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);

        AuthResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("apoderado@gmail.com");
        assertThat(response.getRoles()).contains("ROLE_APODERADO");
    }

    // Registro - Validaciones errores de dominio

    @Test
    void register_throws_whenInvalidDomainForAdminOrDocente() {
        var request = requestBuilder.email("docente@gmail.com").rol(Rol.DOCENTE).build();

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void register_throws_whenInvalidDomainForEstudiante() {
        var request = requestBuilder.email("alumno@gmail.com").rol(Rol.ESTUDIANTE).build();

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void register_throws_whenInvalidDomainForApoderado() {
        var request = requestBuilder.email("apoderado@yahoo.com").rol(Rol.APODERADO).build();

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class);
    }

    // Registro — Correos y RUN duplicados

    @Test
    void register_throws_whenEmailAlreadyExists() {
        var request = requestBuilder.email("docente@colegioohiggins.cl").rol(Rol.DOCENTE).build();

        when(usuarioRepository.existsByEmail(any())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El correo ya está registrado");
    }

    @Test
    void register_throws_whenRutAlreadyExists() {
        var request = requestBuilder.email("docente@colegioohiggins.cl").rol(Rol.DOCENTE).build();

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByRut(any())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Ya existe un usuario registrado con este RUT");
    }

    // Registro — Codificacion de contraseña

    @Test
    void register_encodesPassword() {
        var request = requestBuilder.email("docente@colegioohiggins.cl").rol(Rol.DOCENTE).build();

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByRut(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-pass");
        when(jwtService.generateToken(any())).thenReturn("mock-jwt");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);

        authService.register(request);

        verify(passwordEncoder).encode(any());
    }

    // Login — Exitoso

    @Test
    void authenticate_success() {
        var request = new AuthenticationRequest("docente@colegioohiggins.cl", "Pass123!");
        var usuario = usuarioBuilder.email("docente@colegioohiggins.cl").rol(Rol.DOCENTE).build();

        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(any())).thenReturn("mock-jwt");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);

        AuthResponse response = authService.authenticate(request);

        assertThat(response.getAccessToken()).isEqualTo("mock-jwt");
        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh");
        assertThat(response.getEmail()).isEqualTo("docente@colegioohiggins.cl");
        assertThat(response.getTokenType()).isEqualTo("BEARER");
        assertThat(response.getRoles()).contains("ROLE_DOCENTE");
    }

    // Login — Fallidos

    @Test
    void authenticate_throws_whenBadCredentials() {
        var request = new AuthenticationRequest("docente@colegioohiggins.cl", "wrong-pass");

        doThrow(new BadCredentialsException("bad creds"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Email o contraseña incorrectas");
    }

    @Test
    void authenticate_throws_whenAccountDisabled() {
        var request = new AuthenticationRequest("docente@colegioohiggins.cl", "Pass123!");
        var usuario = usuarioBuilder.email("docente@colegioohiggins.cl").rol(Rol.DOCENTE).build();
        usuario.setEnabled(false);

        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Esta cuenta ha sido desactivada");
    }

    @Test
    void authenticate_throws_whenUserNotFound() {
        var request = new AuthenticationRequest("unknown@colegioohiggins.cl", "Pass123!");

        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuario no encontrado");
    }
}
