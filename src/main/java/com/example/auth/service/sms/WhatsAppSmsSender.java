package com.example.auth.service.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class WhatsAppSmsSender implements SmsSender {
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://graph.facebook.com")
            .build();

    @Value("${WA_API_VERSION}")
    private String apiVersion;

    @Value("${WA_PHONE_ID}")
    private String phoneId;

    @Value("${WA_TOKEN}")
    private String token;

    @Override
    public void sendSms(String to, String text) {
        String path = "/" + apiVersion + "/" + phoneId + "/messages";
        webClient.post()
                .uri(path)
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of(
                        "messaging_product", "whatsapp",
                        "to", to.replace("+", ""),
                        "type", "text",
                        "text", Map.of("body", text)
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
