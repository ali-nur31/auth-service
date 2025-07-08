package com.example.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    WebClient keycloakWebClient(@Value("${keycloak.url}") String url) {
        return WebClient.builder()
                .baseUrl(url)
                .build();
    }
}
