package com.example.auth.security;

import com.example.auth.model.RefreshToken;
import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.accessExpirationMinutes}")
    private Long accessExpMins;
    @Value("${app.jwt.refreshExpirationDays}")
    private Long refreshExpDays;

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getId().toString())
                .claim("tokenVersion", user.getTokenVersion())
                .claim("roles", user.getRoles().stream()
                        .map(Role::getName).toList())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(accessExpMins, ChronoUnit.MINUTES)))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plus(refreshExpDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    public Jws<Claims> validateToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);
    }

    public String extractJwtId(String jwtToken) {
        return validateToken(jwtToken).getBody().getId();
    }

    public long remainingSeconds(String jwtToken) {
        Date expiration = validateToken(jwtToken).getBody().getExpiration();
        return Duration.between(Instant.now(), expiration.toInstant()).toSeconds();
    }
}
