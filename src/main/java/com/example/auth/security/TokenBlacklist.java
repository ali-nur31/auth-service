package com.example.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class TokenBlacklist {
    private final RedisTemplate<String, String> redisTemplate;
    public static final String BLACKLIST_PREFIX = "blacklist:";

    public void add(String jwtId, long secondsToLive) {
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jwtId, "revoked", Duration.ofSeconds(secondsToLive));
    }

    public boolean contains(String jwtId) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + jwtId);
    }
}
