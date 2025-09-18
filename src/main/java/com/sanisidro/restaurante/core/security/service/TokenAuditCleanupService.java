package com.sanisidro.restaurante.core.security.service;

import com.sanisidro.restaurante.core.security.repository.TokenAuditRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenAuditCleanupService {

    private final TokenAuditRepository tokenAuditRepository;

    /**
     * Elimina los registros con más de 12 meses.
     */
    @Transactional
    public void cleanupOldAudits() {
        Instant cutoff = Instant.now().minusSeconds(12 * 30 * 24 * 60 * 60); // ~12 meses
        tokenAuditRepository.deleteByBlacklistedAtBefore(cutoff);
        System.out.println("TokenAudit cleanup executed at " + Instant.now());
    }

}
