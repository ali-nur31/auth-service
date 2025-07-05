package com.example.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginEventListener {
    private final HttpServletRequest httpServletRequest;

    @EventListener
    public void handleSuccess(AuthenticationSuccessEvent event) {
        String user = event.getAuthentication().getName();
        log.info("login_success user={} ip={} agent={}", user, ip(), httpServletRequest.getHeader("User-Agent"));
    }

    @EventListener
    public void handleFailure(AuthenticationFailureBadCredentialsEvent event) {
        String user = event.getAuthentication().getName();
        log.warn("login_fail user={} ip={} agent={}", user, ip(), httpServletRequest.getHeader("User-Agent"));
    }

    private String ip() {
        String forwarded = httpServletRequest.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0] : httpServletRequest.getRemoteAddr();
    }
}
