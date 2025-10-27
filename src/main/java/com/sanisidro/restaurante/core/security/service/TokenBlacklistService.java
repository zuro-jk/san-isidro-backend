package com.sanisidro.restaurante.core.security.service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.security.dto.BlacklistedToken;
import com.sanisidro.restaurante.core.security.enums.TokenEventType;
import com.sanisidro.restaurante.core.security.model.TokenAudit;
import com.sanisidro.restaurante.core.security.repository.TokenAuditRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TokenAuditRepository tokenAuditRepository;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    /**
     * Agrega un token a la blacklist (por ejemplo, al cerrar sesión o por cierre
     * forzado).
     */
    public void blacklistToken(
            String token,
            String username,
            String reason,
            Instant expiresAt,
            String ipAddress,
            String userAgent) {
        // Guardar en Redis con TTL igual al tiempo restante de expiración
        long ttlMillis = Math.max(expiresAt.toEpochMilli() - Instant.now().toEpochMilli(), 0);
        BlacklistedToken data = new BlacklistedToken(token, username, Instant.now(), reason);
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, data, ttlMillis, TimeUnit.MILLISECONDS);

        // Registrar evento en auditoría
        TokenAudit audit = TokenAudit.builder()
                .token(token)
                .username(username)
                .eventType(TokenEventType.BLACKLISTED)
                .timestamp(Instant.now())
                .expiresAt(expiresAt)
                .reason(reason)
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

    @Transactional
    public void revokeAllTokensForRoles(List<String> roleNames) {
        Instant now = Instant.now();

        List<TokenAudit> activeTokens = tokenAuditRepository.findAll().stream()
                .filter(t -> t.getExpiresAt().isAfter(now))
                .filter(t -> t.getEventType() != TokenEventType.BLACKLISTED)
                .filter(t -> {
                    return roleNames.stream().anyMatch(r -> t.getReason() != null && t.getReason().contains(r));
                })
                .toList();

        for (TokenAudit token : activeTokens) {
            token.setEventType(TokenEventType.BLACKLISTED);
            token.setReason("Revocado automáticamente por fin de jornada");
        }

        tokenAuditRepository.saveAll(activeTokens);
    }

}
