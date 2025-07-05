package com.example.auth.service;

import com.example.auth.dto.GenericResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.RegisterRequest;

public interface AuthService {
    GenericResponse register(RegisterRequest registerRequest);
    LoginResponse login(LoginRequest loginRequest);
    void logout(String refreshTokenValue, String accessTokenValue);
    LoginResponse refresh(String oldToken);
}

