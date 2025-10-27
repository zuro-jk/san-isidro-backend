package com.sanisidro.restaurante.core.security.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sanisidro.restaurante.core.security.service.TokenAuditCleanupService;
import com.sanisidro.restaurante.core.security.service.TokenBlacklistService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final TokenAuditCleanupService cleanupService;
    private final TokenBlacklistService tokenBlacklistService;

    // Ejecuta una vez al mes: 00:00 del primer día de cada mes
    @Scheduled(cron = "0 0 0 1 * *")
    public void cleanupOldTokenAudits() {
        cleanupService.cleanupOldAudits();
    }
    
    // Ejecuta todos los días a medianoche
    @Scheduled(cron = "0 0 0 * * *")
    public void revokeEmployeeTokensAtMidnight() {
        tokenBlacklistService.revokeAllTokensForRoles(
                List.of("ROLE_EMPLOYEE", "ROLE_WAITER", "ROLE_CHEF", "ROLE_CASHIER"));
        System.out.println("Revocación automática de sesiones de empleados ejecutada a medianoche");
    }

}
