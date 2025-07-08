package com.example.auth.controller;

import com.example.auth.dto.ConfirmCodeRequest;
import com.example.auth.dto.GenericResponse;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.UserPrincipal;
import com.example.auth.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/verify")
@RequiredArgsConstructor
public class VerificationController {
    private final VerificationService verificationService;
    private final UserRepository userRepository;

    @Value("${bot.username}")
    private String botUsername;

    @GetMapping("/email")
    public GenericResponse confirmEmail(@RequestParam String token) {
        verificationService.verifyCode(token);

        return new GenericResponse("Email address verified");
    }

    @PostMapping("/email")
    public GenericResponse sendEmail(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        verificationService.sendEmailVerificationToken(userRepository.getReferenceById(userPrincipal.getId()));

        return new GenericResponse("Email sent");
    }

    @PostMapping("/telegram/link")
    public GenericResponse start(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        String uuid = verificationService.createTelegramLinkToken(userPrincipal.getId());
        String url = "https://t.me/" + botUsername + "?start=" + uuid;

        return new GenericResponse(url);
    }

    @PostMapping("/telegram/code")
    public GenericResponse confirmCode(@RequestBody ConfirmCodeRequest confirmCodeRequest) {
        verificationService.verifyCode(confirmCodeRequest.getCode());

        return new GenericResponse("Phone verified");
    }
}
