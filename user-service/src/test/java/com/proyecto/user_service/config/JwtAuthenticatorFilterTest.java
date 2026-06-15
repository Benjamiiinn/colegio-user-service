package com.proyecto.user_service.config;

import com.proyecto.user_service.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticatorFilterTest {

    @InjectMocks
    private JwtAuthenticatorFilter filter;

    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // Skip — sin token ni auth header

    @Test
    void doFilterInternal_skipsWhenNoTokenAndNotAuthPath() throws Exception {
        when(jwtService.getJwtFromCookies(request)).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // Skip — URI contiene /auth

    @Test
    void doFilterInternal_skipsWhenAuthPath() throws Exception {
        when(jwtService.getJwtFromCookies(request)).thenReturn("valid-token");
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUserName(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // Autentica con cookie

    @Test
    void doFilterInternal_authenticatesWithCookieToken() throws Exception {
        var userDetails = mock(UserDetails.class);

        when(jwtService.getJwtFromCookies(request)).thenReturn("valid-token");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/usuarios");
        when(jwtService.extractUserName("valid-token")).thenReturn("test@colegioohiggins.cl");
        when(userDetailsService.loadUserByUsername("test@colegioohiggins.cl")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-token", userDetails)).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_DOCENTE"))).when(userDetails).getAuthorities();

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(userDetails);
        assertThat(auth.getAuthorities())
                .extracting(a -> a.getAuthority())
                .contains("ROLE_DOCENTE");

        verify(filterChain).doFilter(request, response);
    }

    // Autentica con Authorization header

    @Test
    void doFilterInternal_authenticatesWithBearerHeader() throws Exception {
        var userDetails = mock(UserDetails.class);

        when(jwtService.getJwtFromCookies(request)).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer header-token");
        when(request.getRequestURI()).thenReturn("/api/v1/usuarios");
        when(jwtService.extractUserName("header-token")).thenReturn("test@colegioohiggins.cl");
        when(userDetailsService.loadUserByUsername("test@colegioohiggins.cl")).thenReturn(userDetails);
        when(jwtService.isTokenValid("header-token", userDetails)).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_DOCENTE"))).when(userDetails).getAuthorities();

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(userDetails);

        verify(filterChain).doFilter(request, response);
    }

    // Skip — token inválido

    @Test
    void doFilterInternal_skipsWhenTokenInvalid() throws Exception {
        var userDetails = mock(UserDetails.class);

        when(jwtService.getJwtFromCookies(request)).thenReturn("invalid-token");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/usuarios");
        when(jwtService.extractUserName("invalid-token")).thenReturn("test@colegioohiggins.cl");
        when(userDetailsService.loadUserByUsername("test@colegioohiggins.cl")).thenReturn(userDetails);
        when(jwtService.isTokenValid("invalid-token", userDetails)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    // Skip — email vacío en el token

    @Test
    void doFilterInternal_skipsWhenEmailEmpty() throws Exception {
        when(jwtService.getJwtFromCookies(request)).thenReturn("empty-email-token");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/usuarios");
        when(jwtService.extractUserName("empty-email-token")).thenReturn("");

        filter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
