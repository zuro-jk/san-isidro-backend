package com.sanisidro.restaurante.core.security.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.sanisidro.restaurante.core.security.repository.TokenAuditRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenAuditCleanupService {

    private final TokenAuditRepository tokenAuditRepository;

    /**
     * 🧹 Elimina los registros de auditoría con más de 12 meses de antigüedad.
     */
    @Transactional
    public void cleanupOldAudits() {
        // 12 meses ≈ 12 * 30 días
        Instant cutoff = Instant.now().minusSeconds(12L * 30 * 24 * 60 * 60);
        tokenAuditRepository.deleteByTimestampBefore(cutoff);

        System.out.println("✅ TokenAudit cleanup ejecutado en: " + Instant.now());
    }

}
