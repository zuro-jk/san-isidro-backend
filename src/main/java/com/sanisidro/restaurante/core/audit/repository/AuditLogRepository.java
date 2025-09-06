package com.sanisidro.restaurante.core.audit.repository;

import com.sanisidro.restaurante.core.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
