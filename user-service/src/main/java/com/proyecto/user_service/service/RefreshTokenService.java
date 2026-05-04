package com.proyecto.user_service.service;

import java.util.Optional;

import org.springframework.http.ResponseCookie;

import com.proyecto.user_service.model.RefreshToken;
import com.proyecto.user_service.request.RefreshTokenRequest;
import com.proyecto.user_service.response.RefreshTokenResponse;

import jakarta.servlet.http.HttpServletRequest;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId);
    RefreshToken verifyExpiration(RefreshToken token);
    Optional<RefreshToken> findByToken(String token);
    RefreshTokenResponse generateNewToken(RefreshTokenRequest request);
    ResponseCookie generateRefreshTokenCookie(String token);
    String getRefreshTokenFromCookies(HttpServletRequest request);
    void deleteByToken(String token);
    ResponseCookie getCleanRefreshTokenCookie();
}
