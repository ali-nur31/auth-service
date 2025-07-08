package com.example.auth.service;

import com.example.auth.model.TokenType;
import com.example.auth.model.User;
import com.example.auth.model.VerificationToken;
import com.example.auth.repository.UserRepository;
import com.example.auth.repository.VerificationTokenRepository;
import com.example.auth.service.email.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final TemplateEngine thymeleafTemplateEngine;

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;

    @Value("${app.server.url}")
    private String serverUrl;

    public String createTelegramLinkToken(Long userId) {
        User user = userRepository.getReferenceById(userId);
        String uuid = UUID.randomUUID().toString();
        verificationTokenRepository.save(new VerificationToken(user, TokenType.TELEGRAM_LINK, uuid, LocalDateTime.now().plusMinutes(30)));

        return uuid;
    }

    @Transactional
    public String processTelegramContact(String linkUuid, String phoneNumber, Long chatId) {
        VerificationToken link = verificationTokenRepository.findByTokenAndConsumedFalse(linkUuid)
                .orElseThrow();
        User user = link.getUser();

        if(!normalizePhoneNumber(phoneNumber).equals(normalizePhoneNumber(user.getPhoneNumber()))) {
            throw new IllegalArgumentException("Phone mismatch");
        }

        link.setConsumed(true);
        user.setTelegramChatId(chatId);

        String verificationCode = "%6d".formatted(ThreadLocalRandom.current().nextInt(1_000_000));
        verificationTokenRepository.save(new VerificationToken(user, TokenType.TELEGRAM_CODE, verificationCode, LocalDateTime.now().plusMinutes(10)));

        return verificationCode;
    }

    @Transactional
    public void verifyCode(String verificationCode) {
        VerificationToken verificationToken = verificationTokenRepository.findByTokenAndConsumedFalse(verificationCode)
                .orElseThrow();

        if(verificationToken.getExpiryAt().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Verification code expired");

        if (verificationToken.getType() == TokenType.TELEGRAM_CODE) {
            verificationToken.getUser().setPhoneVerified(true);
        } else if (verificationToken.getType() == TokenType.EMAIL) {
            verificationToken.getUser().setEmailVerified(true);
        }

        verificationToken.setConsumed(true);
    }

    @Transactional
    public void sendEmailVerificationToken(User user) {
        verificationTokenRepository.findActiveByUserAndType(user, TokenType.EMAIL)
                .ifPresent(verificationToken -> verificationToken.setConsumed(true));

        String uuid = UUID.randomUUID().toString();

        verificationTokenRepository.save(new VerificationToken(user, TokenType.EMAIL, uuid, LocalDateTime.now().plusHours(24)));

        String link = serverUrl + "/api/verify/email?token=" + uuid;

        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("link", link);

        String html = thymeleafTemplateEngine.process("verification-email.html", context);

        emailSender.sendEmail(user.getEmail(), "Email confirmation", html);
    }

    private static String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^0-9]", "");
    }
}
