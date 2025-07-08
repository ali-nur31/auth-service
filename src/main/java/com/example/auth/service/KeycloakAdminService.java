package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakAdminService {
    private final WebClient webClient;
    @Value("${keycloak.realm}")
    String realm;
    @Value("${keycloak.admin-user}")
    String adminUser;
    @Value("${keycloak.admin-password}")
    String adminPassword;

    public Mono<String> adminToken() {
        return webClient.post()
                .uri("/realms/master/protocol/openid-connect/token")
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "admin-cli")
                        .with("username", adminUser)
                        .with("password", adminPassword))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> jsonNode.get("access_token").asText());
    }

    public Mono<Void> createUser(RegisterRequest registerRequest) {
        return adminToken().flatMap(token ->
                webClient.post()
                        .uri("admin/realms/{realm}/users", realm)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of(
                            "username", registerRequest.getUsername(),
                            "email", registerRequest.getEmail(),
                            "enabled", true,
                            "credentials", List.of(Map.of(
                                        "type", "password",
                                        "value", registerRequest.getPassword(),
                                        "temporary", false
                                )),
                            "realmRoles", List.of("ROLE_USER")
                        ))
                        .retrieve().toBodilessEntity().then());
    }

    public Mono<LoginResponse> loginUser(LoginRequest loginRequest) {
        return webClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "auth-service")
                        .with("username", loginRequest.getUsernameOrEmail())
                        .with("password", loginRequest.getPassword()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode ->
                        new LoginResponse(
                                jsonNode.get("access_token").asText(),
                                jsonNode.get("refresh_token").asText(),
                                jsonNode.get("expires_in").asLong()
                        )
                );
    }

    public Mono<LoginResponse> validateRefreshToken(String refreshToken) {
        return webClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", "auth-service")
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode ->
                        new LoginResponse(
                                jsonNode.get("access_token").asText(),
                                jsonNode.get("refresh_token").asText(),
                                jsonNode.get("expires_in").asLong()
                        )
                );
    }

    public Mono<Void> logoutUser(String refreshToken) {
        return webClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/logout", realm)
                .body(BodyInserters.fromFormData("client_id", "auth-service")
                        .with("refresh_token", refreshToken))
                .retrieve().toBodilessEntity().then();
    }
}
