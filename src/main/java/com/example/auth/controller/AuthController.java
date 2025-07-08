package com.example.auth.controller;

import com.example.auth.dto.GenericResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.service.KeycloakAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final KeycloakAdminService keycloakAdminService;

    @PostMapping("/register")
    public Mono<GenericResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return keycloakAdminService.createUser(registerRequest)
                .thenReturn(new GenericResponse("User registered, verify email"));
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return keycloakAdminService.loginUser(loginRequest);
    }

    @PostMapping("/refresh")
    public Mono<LoginResponse> refresh(@RequestParam String refreshToken) {
        return keycloakAdminService.validateRefreshToken(refreshToken);
    }

    @PostMapping("/logout")
    public Mono<GenericResponse> logout(@RequestParam String refreshToken) {
        return keycloakAdminService.logoutUser(refreshToken)
                .thenReturn(new GenericResponse("User logged out successfully"));
    }
}
