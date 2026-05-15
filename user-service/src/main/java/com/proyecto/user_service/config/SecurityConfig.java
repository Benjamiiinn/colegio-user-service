package com.proyecto.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticatorFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;
    private final Http401UnauthorizedEntryPoint unauthorizedEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(request -> request
                    .requestMatchers("/api/v1/auth/**", "/api/auth/**", "/error").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/*/exists").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/**").hasAnyRole("ADMIN", "DOCENTE", "ESTUDIANTE")
                    .anyRequest().authenticated()
                )
            .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS)) 
            .authenticationProvider(authProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();    
    }
}
