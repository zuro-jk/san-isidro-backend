package com.sanisidro.restaurante.core.security.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sanisidro.restaurante.core.security.model.TokenAudit;

import jakarta.transaction.Transactional;

public interface TokenAuditRepository extends JpaRepository<TokenAudit, Long> {

    /**
     * Elimina registros de auditoría antiguos según su timestamp.
     */
    @Transactional
    void deleteByTimestampBefore(Instant cutoffDate);

    /**
     * Encuentra tokens activos (no expirados y no en lista negra).
     */
    @Query("""
                SELECT t.token FROM TokenAudit t
                WHERE t.username = :username
                AND t.expiresAt > :now
                AND t.eventType <> com.sanisidro.restaurante.core.security.enums.TokenEventType.BLACKLISTED
            """)
    List<String> findActiveTokensByUsername(@Param("username") String username, @Param("now") Instant now);

}
