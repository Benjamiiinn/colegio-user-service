package com.proyecto.user_service.service.impl;

import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import com.proyecto.user_service.enums.TokenType;
import com.proyecto.user_service.exception.TokenException;
import com.proyecto.user_service.model.RefreshToken;
import com.proyecto.user_service.model.Usuario;
import com.proyecto.user_service.repository.RefreshTokenRepository;
import com.proyecto.user_service.repository.UsuarioRepository;
import com.proyecto.user_service.request.RefreshTokenRequest;
import com.proyecto.user_service.response.RefreshTokenResponse;
import com.proyecto.user_service.service.JwtService;
import com.proyecto.user_service.service.RefreshTokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private Long refreshExpiration;

    @Value("${application.security.jwt.refresh-token.cookie-name}")
    private String refreshTokenName;

    @Override
    public RefreshToken createRefreshToken(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        RefreshToken refreshToken = RefreshToken.builder()
                .revoked(false)
                .usuario(usuario)
                .token(Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes()))
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token == null) {
            log.error("Token no encontrado");
            throw new TokenException(null, "Token no encontrado");
        }
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenException(token.getToken(), "Token expirado, por favor inicia sesión de nuevo");
        }
        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshTokenResponse generateNewToken(RefreshTokenRequest request) {
        Usuario usuario = refreshTokenRepository.findByToken(request.getRefreshToken())
                .map(this::verifyExpiration)
                .map(RefreshToken::getUsuario)
                .orElseThrow(() -> new TokenException(request.getRefreshToken(), "Token no válido"));

        String token = jwtService.generateToken(usuario);
        return RefreshTokenResponse.builder()
                .accessToken(token)
                .refreshToken(request.getRefreshToken())
                .tokenType(TokenType.BEARER.name())
                .build();
    }

    @Override
    public ResponseCookie generateRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(refreshTokenName, refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(15 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }

    @Override
    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, refreshTokenName);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return "";
        }
    }

    @Override
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    @Override
    public ResponseCookie getCleanRefreshTokenCookie() {
        return ResponseCookie.from(refreshTokenName, "")
            .path("/")
            .httpOnly(true)
            .maxAge(0)
            .build();
    }
}
