package com.proyecto.user_service.service.impl;

import com.proyecto.user_service.enums.Rol;
import com.proyecto.user_service.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private Usuario usuario;

    private static final String SECRET_KEY = "dGhpcyBpcyBhIHNlY3JldCBrZXkgZm9yIEpXVCB0ZXN0aW5nIHB1cnBvc2VzIDEyMzQ1Njc4OTA=";
    private static final long JWT_EXPIRATION = 900000L;
    private static final long REFRESH_EXPIRATION = 1296000000L;
    private static final String COOKIE_NAME = "jwt-cookie";

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();

        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "JwtCookieName", COOKIE_NAME);

        usuario = Usuario.builder()
                .id(1L)
                .email("test@colegioohiggins.cl")
                .password("Pass123!")
                .rol(Rol.DOCENTE)
                .build();
    }

    // Genera token y extrae claims

    @Test
    void generateToken_createsValidJwt() {
        String token = jwtService.generateToken(usuario);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateToken_containsRolesAndUserId() {
        String token = jwtService.generateToken(usuario);

        Claims claims = ReflectionTestUtils.invokeMethod(jwtService, "extractAllClaims", token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("test@colegioohiggins.cl");
        assertThat(claims.get("roles")).asList().contains("ROLE_DOCENTE");
        assertThat(claims.get("userId")).isEqualTo(1);
    }

    // Extrae el Email del token

    @Test
    void extractUserName_returnsEmail() {
        String token = jwtService.generateToken(usuario);

        String username = jwtService.extractUserName(token);

        assertThat(username).isEqualTo("test@colegioohiggins.cl");
    }

    // Valida si el token es valido

    @Test
    void isTokenValid_returnsTrue() {
        String token = jwtService.generateToken(usuario);

        boolean result = jwtService.isTokenValid(token, usuario);

        assertThat(result).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_whenWrongUser() {
        var otroUsuario = Usuario.builder()
                .id(2L)
                .email("otro@colegioohiggins.cl")
                .rol(Rol.DOCENTE)
                .build();

        String token = jwtService.generateToken(usuario);

        boolean result = jwtService.isTokenValid(token, otroUsuario);

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_whenExpired() throws Exception {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);

        String token = jwtService.generateToken(usuario);
        
        assertThatThrownBy(() -> jwtService.isTokenValid(token, usuario))
                .isInstanceOf(ExpiredJwtException.class);
    }

    // Genera jwt en cookie y limpia cookie

    @Test
    void generateJwtCookie_createsCookie() {
        ResponseCookie cookie = jwtService.generateJwtCookie("test-jwt");

        assertThat(cookie.getName()).isEqualTo("jwt-cookie");
        assertThat(cookie.getValue()).isEqualTo("test-jwt");
        assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(86400L);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
    }

    @Test
    void getCleanJwtCookie_createsCookie() {
        ResponseCookie cookie = jwtService.getCleanJwtCookie();

        assertThat(cookie.getName()).isEqualTo("jwt-cookie");
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge().getSeconds()).isZero();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
    }

    // Extrae el JWT de las cookies

    @Test
    void getJwtFromCookies_returnsToken() {
        var request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("jwt-cookie", "test-token")});

        String result = jwtService.getJwtFromCookies(request);

        assertThat(result).isEqualTo("test-token");
    }

    @Test
    void getJwtFromCookies_returnsNull() {
        var request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        String result = jwtService.getJwtFromCookies(request);

        assertThat(result).isNull();
    }
}
