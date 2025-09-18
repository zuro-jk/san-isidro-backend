package com.sanisidro.restaurante.core.security.service;

import com.sanisidro.restaurante.core.security.dto.BlacklistedToken;
import com.sanisidro.restaurante.core.security.model.TokenAudit;
import com.sanisidro.restaurante.core.security.repository.TokenAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TokenAuditRepository tokenAuditRepository;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    /**
     * Guarda un token en blacklist con información adicional.
     */
    public void blacklistToken(String token, String username, String reason, long expirationMillis, String ipAddress, String userAgent) {

        BlacklistedToken data = new BlacklistedToken(token, username, Instant.now(), reason);
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, data, expirationMillis, TimeUnit.MILLISECONDS);

        TokenAudit audit = TokenAudit.builder()
                .token(token)
                .username(username)
                .reason(reason)
                .blacklistedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(expirationMillis))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        tokenAuditRepository.save(audit);
    }

    /**
     * Verifica si un token está en blacklist.
     */
    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }

    /**
     * Obtiene la información del token en blacklist.
     */
    public BlacklistedToken getBlacklistedInfo(String token) {
        Object value = redisTemplate.opsForValue().get(BLACKLIST_PREFIX + token);
        if (value instanceof BlacklistedToken bt) {
            return bt;
        }
        return null;
    }

}
