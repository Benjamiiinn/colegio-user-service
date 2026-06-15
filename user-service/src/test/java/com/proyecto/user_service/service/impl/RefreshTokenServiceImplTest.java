package com.proyecto.user_service.service.impl;

import com.proyecto.user_service.enums.Rol;
import com.proyecto.user_service.exception.TokenException;
import com.proyecto.user_service.model.RefreshToken;
import com.proyecto.user_service.model.Usuario;
import com.proyecto.user_service.repository.RefreshTokenRepository;
import com.proyecto.user_service.repository.UsuarioRepository;
import com.proyecto.user_service.request.RefreshTokenRequest;
import com.proyecto.user_service.response.RefreshTokenResponse;
import com.proyecto.user_service.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;

    private Usuario usuario;
    private static final long FIFTEEN_DAYS = 15L * 24 * 60 * 60 * 1000;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", FIFTEEN_DAYS);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenName", "refresh-jwt-cookie");

        usuario = Usuario.builder()
                .id(1L)
                .email("test@colegioohiggins.cl")
                .rol(Rol.DOCENTE)
                .build();
    }

    // Crear Token de refresco

    @Test
    void createRefreshToken_success() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(1L);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getUsuario()).isEqualTo(usuario);
        assertThat(result.isRevoked()).isFalse();
        assertThat(result.getExpiryDate()).isAfter(Instant.now());
    }

    @Test
    void createRefreshToken_throws_whenUserNotFound() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.createRefreshToken(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");
    }

    // Verifica Expiracion del Token

    @Test
    void verifyExpiration_throws_whenTokenNull() {
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(null))
                .isInstanceOf(TokenException.class)
                .hasMessage("Failed for [null]: Token no encontrado");
    }

    @Test
    void verifyExpiration_throws_whenExpired() {
        var expiredToken = RefreshToken.builder()
                .token("expired-token")
                .expiryDate(Instant.now().minusSeconds(10))
                .build();

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(expiredToken))
                .isInstanceOf(TokenException.class)
                .hasMessage("Failed for [expired-token]: Token expirado, por favor inicia sesión de nuevo");

        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    void verifyExpiration_returnsToken_whenValid() {
        var validToken = RefreshToken.builder()
                .token("valid-token")
                .expiryDate(Instant.now().plusSeconds(10000))
                .build();

        RefreshToken result = refreshTokenService.verifyExpiration(validToken);

        assertThat(result).isEqualTo(validToken);
        verify(refreshTokenRepository, never()).delete(any());
    }

    // Genera un nuevo Token de acceso

    @Test
    void generateNewToken_success() {
        var request = new RefreshTokenRequest("valid-token");
        var token = RefreshToken.builder()
                .token("valid-token")
                .usuario(usuario)
                .expiryDate(Instant.now().plusSeconds(10000))
                .build();

        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(jwtService.generateToken(usuario)).thenReturn("new-jwt");

        RefreshTokenResponse response = refreshTokenService.generateNewToken(request);

        assertThat(response.getAccessToken()).isEqualTo("new-jwt");
        assertThat(response.getRefreshToken()).isEqualTo("valid-token");
        assertThat(response.getTokenType()).isEqualTo("BEARER");
    }

    @Test
    void generateNewToken_throws_whenTokenInvalid() {
        var request = new RefreshTokenRequest("invalid-token");

        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.generateNewToken(request))
                .isInstanceOf(TokenException.class)
                .hasMessage("Failed for [invalid-token]: Token no válido");
    }

    // Borrar Token

    @Test
    void deleteByToken_deletesWhenFound() {
        var token = RefreshToken.builder().token("test-token").build();
        when(refreshTokenRepository.findByToken("test-token")).thenReturn(Optional.of(token));

        refreshTokenService.deleteByToken("test-token");

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void deleteByToken_doesNothingWhenNotFound() {
        when(refreshTokenRepository.findByToken("test-token")).thenReturn(Optional.empty());

        refreshTokenService.deleteByToken("test-token");

        verify(refreshTokenRepository, never()).delete(any());
    }
}
