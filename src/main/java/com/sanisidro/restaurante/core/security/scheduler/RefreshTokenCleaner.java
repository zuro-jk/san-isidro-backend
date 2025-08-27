package com.sanisidro.restaurante.core.security.scheduler;

import com.sanisidro.restaurante.core.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleaner {

    private final RefreshTokenRepository refreshTokenRepository;

    // Se ejecuta cada 1 hora
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
    }
}
