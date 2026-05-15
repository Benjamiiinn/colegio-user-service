package com.proyecto.user_service.service.impl;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.user_service.enums.Rol;
import com.proyecto.user_service.enums.TokenType;
import com.proyecto.user_service.exception.BusinessRuleException;
import com.proyecto.user_service.exception.ResourceNotFoundException;
import com.proyecto.user_service.model.Usuario;
import com.proyecto.user_service.repository.UsuarioRepository;
import com.proyecto.user_service.request.AuthenticationRequest;
import com.proyecto.user_service.request.RegisterRequest;
import com.proyecto.user_service.response.AuthResponse;
import com.proyecto.user_service.service.AuthService;
import com.proyecto.user_service.service.JwtService;
import com.proyecto.user_service.service.RefreshTokenService;
import com.proyecto.user_service.util.RutUtils;

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
                        throw new BusinessRuleException("Los docentes y administradores deben usar el dominio @colegioohiggins.cl");
                }
        } else if (rolSolicitado == Rol.ESTUDIANTE) {
                if (!email.endsWith("@alumnos.colegioohiggins.cl")) {
                        throw new BusinessRuleException("Los estudiantes deben usar el dominio @alumnos.colegioohiggins.cl");
                }
        } else if (rolSolicitado == Rol.APODERADO) {
                if (!email.endsWith("@gmail.com") && !email.endsWith("@gmail.cl")) {
                        throw new BusinessRuleException("Los apoderados deben usar correo gmail.com o gmail.cl");
                }
        }

        if (usuarioRepository.existsByEmail(email)) {
                throw new BusinessRuleException("El correo ya está registrado");
        }

        if (usuarioRepository.existsByRut(request.getRut())) {
                throw new BusinessRuleException("Ya existe un usuario registrado con este RUT");
        }

        String rutNormalizado = RutUtils.formatearRut(request.getRut());

        var usuario = Usuario.builder()
                .rut(rutNormalizado)
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
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Email o contraseña incorrectas");
        } catch (Exception e) {
                throw new BusinessRuleException("Esta cuenta ha sido desactivada");
        }

        var usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
                
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
