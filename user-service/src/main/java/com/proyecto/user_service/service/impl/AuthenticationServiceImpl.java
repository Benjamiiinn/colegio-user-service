package com.proyecto.user_service.service.impl;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.user_service.enums.Rol;
import com.proyecto.user_service.enums.TokenType;
import com.proyecto.user_service.model.Usuario;
import com.proyecto.user_service.repository.UsuarioRepository;
import com.proyecto.user_service.request.AuthenticationRequest;
import com.proyecto.user_service.request.RegisterRequest;
import com.proyecto.user_service.response.AuthResponse;
import com.proyecto.user_service.service.AuthService;
import com.proyecto.user_service.service.JwtService;
import com.proyecto.user_service.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthService{

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        Rol rolSolicitado = request.getRol();
        String email = request.getEmail().toLowerCase();

        if (rolSolicitado == Rol.DOCENTE || rolSolicitado == Rol.ADMIN) {
                if (!email.endsWith("@colegioohiggins.cl")) {
                        throw new IllegalArgumentException("Los docentes y administradores deben usar el dominio @colegioohiggins.cl");
                }
        } else if (rolSolicitado == Rol.ESTUDIANTE) {
                if (!email.endsWith("@alumnos.colegioohiggins.cl")) {
                        throw new IllegalArgumentException("Los estudiantes deben usar el dominio @alumnos.colegioohiggins.cl");
                }
        }

        if (usuarioRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("El correo ya está registrado");
        }

        var usuario = Usuario.builder()
                .rut(request.getRut())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(rolSolicitado)
                .build();
        
        usuarioRepository.save(usuario);

        var jwt = jwtService.generateToken(usuario);
        var refreshToken = refreshTokenService.createRefreshToken(usuario.getId());
        var roles = usuario.getRol().getAuthorities()
                .stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .toList();
        return AuthResponse.builder()
                .accessToken(jwt)
                .email(usuario.getEmail())
                .id(usuario.getId())
                .refreshToken(refreshToken.getToken())
                .roles(roles)
                .tokenType(TokenType.BEARER.name())
                .build();
    }

    @Override
    public AuthResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        var usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectas"));
        var roles = usuario.getRol().getAuthorities()
                .stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .toList();
        var jwt = jwtService.generateToken(usuario);
        var refreshToken = refreshTokenService.createRefreshToken(usuario.getId());
        return AuthResponse.builder()
                .accessToken(jwt)
                .roles(roles)
                .email(usuario.getEmail())
                .id(usuario.getId())
                .refreshToken(refreshToken.getToken())
                .tokenType(TokenType.BEARER.name())
                .build();

    }
}
