package com.example.auth.repository;

import com.example.auth.model.TokenType;
import com.example.auth.model.User;
import com.example.auth.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndConsumedFalse(String token);
    Optional<VerificationToken> findActiveByUserAndType(User user, TokenType type);
    Optional<VerificationToken> findFirstByUserAndTypeAndConsumedFalse(User user, TokenType type);
}
