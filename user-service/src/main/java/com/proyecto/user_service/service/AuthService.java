package com.proyecto.user_service.service;

import com.proyecto.user_service.request.AuthenticationRequest;
import com.proyecto.user_service.request.RegisterRequest;
import com.proyecto.user_service.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse authenticate(AuthenticationRequest request);
}
