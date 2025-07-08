package com.example.auth.service.sms;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@Profile("dev")
@RequiredArgsConstructor
public class TelegramSmsSender implements SmsSender{
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.telegram.org")
            .build();

    @Value("${bot.token}")
    private String token;

    @Override
    public void sendSms(String chatId, String text) {
        webClient.post()
                .uri("/bot" + token + "/sendMessage")
                .bodyValue(Map.of(
                        "chat_id", chatId,
                        "text", text
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
