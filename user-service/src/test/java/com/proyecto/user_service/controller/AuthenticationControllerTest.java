package com.proyecto.user_service.controller;

import com.proyecto.user_service.enums.Rol;
import com.proyecto.user_service.model.Usuario;
import com.proyecto.user_service.response.AuthResponse;
import com.proyecto.user_service.response.RefreshTokenResponse;
import com.proyecto.user_service.service.AuthService;
import com.proyecto.user_service.service.JwtService;
import com.proyecto.user_service.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    private AuthResponse authResponse;
    private Usuario usuario;
    private ResponseCookie jwtCookie;
    private ResponseCookie refreshCookie;

    @BeforeEach
    void setUp() {
        authResponse = AuthResponse.builder()
                .accessToken("mock-jwt")
                .refreshToken("mock-refresh")
                .email("test@colegioohiggins.cl")
                .id(1L)
                .roles(List.of("ROLE_DOCENTE"))
                .tokenType("BEARER")
                .build();

        usuario = Usuario.builder()
                .id(1L)
                .rut("21719226-9")
                .nombres("Juan")
                .apellidos("Perez")
                .email("test@colegioohiggins.cl")
                .password("Pass123!")
                .rol(Rol.DOCENTE)
                .enabled(true)
                .build();

        jwtCookie = ResponseCookie.from("jwt-cookie", "mock-jwt")
                .path("/").httpOnly(true).maxAge(86400).sameSite("Lax").build();

        refreshCookie = ResponseCookie.from("refresh-jwt-cookie", "mock-refresh")
                .path("/").httpOnly(true).maxAge(1296000).sameSite("Lax").build();
    }

    // POST /api/v1/auth/register

    @Test
    void register_returns200WithCookiesAndBody() throws Exception {
        when(authService.register(any())).thenReturn(authResponse);
        when(jwtService.generateJwtCookie("mock-jwt")).thenReturn(jwtCookie);
        when(refreshTokenService.generateRefreshTokenCookie("mock-refresh")).thenReturn(refreshCookie);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .with(user(usuario))
                        .contentType("application/json")
                        .content("""
                                {
                                    "rut": "25041654-7",
                                    "nombres": "Juan",
                                    "apellidos": "Perez",
                                    "email": "test@colegioohiggins.cl",
                                    "password": "Password123!",
                                    "rol": "DOCENTE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@colegioohiggins.cl"))
                .andExpect(jsonPath("$.access_token").value("mock-jwt"))
                .andExpect(cookie().value("jwt-cookie", "mock-jwt"))
                .andExpect(cookie().value("refresh-jwt-cookie", "mock-refresh"));
    }

    // POST /api/v1/auth/authenticate

    @Test
    void authenticate_returns200WithCookiesAndBody() throws Exception {
        when(authService.authenticate(any())).thenReturn(authResponse);
        when(jwtService.generateJwtCookie("mock-jwt")).thenReturn(jwtCookie);
        when(refreshTokenService.generateRefreshTokenCookie("mock-refresh")).thenReturn(refreshCookie);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .with(csrf())
                        .with(user(usuario))
                        .contentType("application/json")
                        .content("""
                                {
                                    "email": "test@colegioohiggins.cl",
                                    "password": "Pass123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@colegioohiggins.cl"))
                .andExpect(jsonPath("$.access_token").value("mock-jwt"))
                .andExpect(cookie().value("jwt-cookie", "mock-jwt"))
                .andExpect(cookie().value("refresh-jwt-cookie", "mock-refresh"));
    }

    // POST /api/v1/auth/refresh-token

    @Test
    void refreshToken_returns200() throws Exception {
        var response = RefreshTokenResponse.builder()
                .accessToken("new-jwt")
                .refreshToken("old-refresh")
                .tokenType("BEARER")
                .build();

        when(refreshTokenService.generateNewToken(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .with(csrf())
                        .with(user(usuario))
                        .contentType("application/json")
                        .content("""
                                {
                                    "refreshToken": "old-refresh"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new-jwt"))
                .andExpect(jsonPath("$.refresh_token").value("old-refresh"))
                .andExpect(jsonPath("$.token_type").value("BEARER"));
    }

    // POST /api/v1/auth/refresh-token-cookie

    @Test
    void refreshTokenCookie_returns200WithNewJwtCookie() throws Exception {
        var response = RefreshTokenResponse.builder()
                .accessToken("new-jwt")
                .refreshToken("old-refresh")
                .tokenType("BEARER")
                .build();

        when(refreshTokenService.getRefreshTokenFromCookies(any())).thenReturn("old-refresh");
        when(refreshTokenService.generateNewToken(any())).thenReturn(response);
        when(jwtService.generateJwtCookie("new-jwt")).thenReturn(
                ResponseCookie.from("jwt-cookie", "new-jwt")
                        .path("/").httpOnly(true).maxAge(86400).build());

        mockMvc.perform(post("/api/v1/auth/refresh-token-cookie")
                        .with(csrf())
                        .with(user(usuario))
                        .cookie(new Cookie("refresh-jwt-cookie", "old-refresh")))
                .andExpect(status().isOk())
                .andExpect(cookie().value("jwt-cookie", "new-jwt"));
    }
    
    // POST /api/v1/auth/logout

    @Test
    void logout_returns200AndClearsCookies() throws Exception {
        var cleanJwt = ResponseCookie.from("jwt-cookie", "")
                .path("/").httpOnly(true).maxAge(0).build();
        var cleanRefresh = ResponseCookie.from("refresh-jwt-cookie", "")
                .path("/").httpOnly(true).maxAge(0).build();

        when(refreshTokenService.getRefreshTokenFromCookies(any())).thenReturn("old-refresh");
        when(jwtService.getCleanJwtCookie()).thenReturn(cleanJwt);
        when(refreshTokenService.getCleanRefreshTokenCookie()).thenReturn(cleanRefresh);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf())
                        .with(user(usuario)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("jwt-cookie", ""))
                .andExpect(cookie().value("refresh-jwt-cookie", ""));

        verify(refreshTokenService).deleteByToken("old-refresh");
    }
}
