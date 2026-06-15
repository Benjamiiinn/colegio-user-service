package com.proyecto.user_service.handlers;

import com.proyecto.user_service.exception.BusinessRuleException;
import com.proyecto.user_service.exception.ResourceNotFoundException;
import com.proyecto.user_service.exception.TokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
    }

    // TokenException = Error 403 Forbidden

    @Test
    void handleTokenException_returns403() {
        var ex = new TokenException("mock-token", "Token inválido");

        ResponseEntity<ErrorResponse> response = handler.handleRefreshTokenException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("Forbidden");
        assertThat(response.getBody().getMessage()).contains("Token inválido");
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/v1/test");
    }

    // BusinessRuleException - Error 400 Bad Request

    @Test
    void handleBusinessRuleException_returns400() {
        var ex = new BusinessRuleException("Regla de negocio violada");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessRuleException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Regla de negocio violada");
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/v1/test");
    }

    // BadCredentialsException = Error 401 Unauthorized

    @Test
    void handleBadCredentialsException_returns401() {
        var ex = new BadCredentialsException("Credenciales inválidas");

        ResponseEntity<ErrorResponse> response = handler.handleBadCredentialsException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getMessage()).isEqualTo("Credenciales inválidas");
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/v1/test");
    }

    // ResourceNotFoundException = Error 404 Not Found

    @Test
    void handleResourceNotFoundException_returns404() {
        var ex = new ResourceNotFoundException("Usuario no encontrado");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("Usuario no encontrado");
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/v1/test");
    }

    // MethodArgumentNotValidException = Error 400 Bad Request

    @Test
    void handleValidationException_returns400() {
        var ex = mock(MethodArgumentNotValidException.class);
        var bindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("registerRequest", "email", "El email no es válido");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Errores de validación: email: El email no es válido");
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/v1/test");
    }

    // HttpMessageNotReadableException = Error 400 Bad Request

    @Test
    void handleHttpMessageNotReadable_returns400() {
        var ex = mock(HttpMessageNotReadableException.class);
        var cause = new Throwable("JSON parse error: nombre inválido");

        when(ex.getMostSpecificCause()).thenReturn(cause);

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Error al parsear JSON: JSON parse error: nombre inválido");
        assertThat(response.getBody().getPath()).isEqualTo("uri=/api/v1/test");
    }
}
