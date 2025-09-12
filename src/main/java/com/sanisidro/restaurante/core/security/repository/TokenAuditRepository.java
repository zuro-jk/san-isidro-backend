package com.sanisidro.restaurante.core.security.repository;

import com.sanisidro.restaurante.core.security.model.TokenAudit;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface TokenAuditRepository extends JpaRepository<TokenAudit, Long> {

    @Transactional
    void deleteByBlacklistedAtBefore(Instant cutoffDate);

}
