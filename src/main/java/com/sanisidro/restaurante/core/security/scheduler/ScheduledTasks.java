package com.sanisidro.restaurante.core.security.scheduler;

import com.sanisidro.restaurante.core.security.service.TokenAuditCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final TokenAuditCleanupService cleanupService;

    // Ejecuta una vez al mes: 00:00 del primer día de cada mes
    @Scheduled(cron = "0 0 0 1 * *")
    public void cleanupOldTokenAudits() {
        cleanupService.cleanupOldAudits();
    }

}
