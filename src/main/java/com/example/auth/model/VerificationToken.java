package com.example.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
@Data
@RequiredArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    private TokenType type;

    private String token;

    private LocalDateTime expiryAt;

    private boolean consumed = false;

    public VerificationToken(User user, TokenType type, String token, LocalDateTime expiryAt) {
        this.user = user;
        this.type = type;
        this.token = token;
        this.expiryAt = expiryAt;
    }
}