package com.example.auth.config;

import com.example.auth.util.PemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtKeyConfig {
    @Value("${app.jwt.privateKey}")
    private Resource privateKeyRes;

    @Value("${app.jwt.publicKey}")
    private Resource publicKeyRes;

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        try (var in = privateKeyRes.getInputStream()) {
            return (RSAPrivateKey) PemUtils.readPrivateKey(in, "RSA");
        }
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        try (var in = publicKeyRes.getInputStream()) {
            return (RSAPublicKey) PemUtils.readPublicKey(in, "RSA");
        }
    }
}
