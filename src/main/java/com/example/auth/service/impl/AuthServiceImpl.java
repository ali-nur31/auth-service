package com.example.auth.service.impl;

import com.example.auth.dto.GenericResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.exception.DuplicateFieldException;
import com.example.auth.exception.GlobalExceptionHandler;
import com.example.auth.model.RefreshToken;
import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtProvider;
import com.example.auth.security.TokenBlacklist;
import com.example.auth.service.AuthService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenBlacklist tokenBlacklist;

    @Override
    @Transactional
    public GenericResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new DuplicateFieldException("username", "Username is already taken!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateFieldException("email", "Email is already in use!");
        }
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BadCredentialsException("Passwords do not match!");
        }

        User user = new User();
        user.setName(registerRequest.getUsername());
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEnabled(true);
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));
        user.getRoles().add(userRole);
        userRepository.save(user);

        return new GenericResponse("User registered successfully!");
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(loginRequest.getUsernameOrEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Username or email doesn't exist!"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect password!");
        }

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken);
    }

    @Override
    public void logout(String refreshTokenValue, String accessTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new JwtException("Token not found!"));
        if (refreshToken.getRevoked()) throw new JwtException("Token is revoked!");
        refreshToken.setRevoked(true);

        String jwtId = jwtProvider.extractJwtId(accessTokenValue);
        long secondsToLive = jwtProvider.remainingSeconds(accessTokenValue);
        tokenBlacklist.add(jwtId, secondsToLive);
    }

    @Override
    public LoginResponse refresh(String oldToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(oldToken)
                .filter(token -> !token.getRevoked())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token!"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now()))
            throw  new IllegalArgumentException("Refresh token expired!");

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        User user = refreshToken.getUser();
        String accessToken = jwtProvider.generateAccessToken(user);
        String newRefreshToken = jwtProvider.generateRefreshToken(user);

        return new LoginResponse(accessToken, newRefreshToken);
    }
}
