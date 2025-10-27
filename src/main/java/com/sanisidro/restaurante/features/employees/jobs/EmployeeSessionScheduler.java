package com.sanisidro.restaurante.features.employees.jobs;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sanisidro.restaurante.core.security.enums.TokenEventType;
import com.sanisidro.restaurante.core.security.model.TokenAudit;
import com.sanisidro.restaurante.core.security.repository.RefreshTokenRepository;
import com.sanisidro.restaurante.core.security.repository.TokenAuditRepository;
import com.sanisidro.restaurante.core.security.service.TokenBlacklistService;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import com.sanisidro.restaurante.features.employees.service.ScheduleService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmployeeSessionScheduler {

    private final EmployeeRepository employeeRepository;
    private final ScheduleService scheduleService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final TokenAuditRepository tokenAuditRepository;

    /**
     * 🔁 Revisa cada 10 minutos si hay empleados fuera de horario laboral.
     */
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void revokeSessionsForEmployeesOutOfSchedule() {
        List<Employee> employees = employeeRepository.findAll();

        for (Employee employee : employees) {
            var user = employee.getUser();

            // 🔹 Excluir usuarios con rol de administrador
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equalsIgnoreCase("ROLE_ADMIN"));
            if (isAdmin)
                continue;

            // 🔹 Validar si está dentro de horario
            boolean dentroHorario = scheduleService.isWithinSchedule(employee);

            if (!dentroHorario) {
                var tokens = refreshTokenRepository.findAllByUser(user);
                if (!tokens.isEmpty()) {
                    for (var token : tokens) {
                        tokenBlacklistService.blacklistToken(
                                token.getToken(),
                                user.getUsername(),
                                "fuera_de_horario",
                                token.getExpiryDate(),
                                token.getIp(),
                                token.getUserAgent());

                        tokenAuditRepository.save(TokenAudit.builder()
                                .token(token.getToken())
                                .username(user.getUsername())
                                .eventType(TokenEventType.REVOKED)
                                .timestamp(Instant.now())
                                .expiresAt(token.getExpiryDate())
                                .reason("Sesión revocada automáticamente por fin de turno laboral")
                                .ipAddress(token.getIp())
                                .userAgent(token.getUserAgent())
                                .build());
                    }
                    refreshTokenRepository.deleteAll(tokens);
                }
            }
        }
    }
}
