package com.sanisidro.restaurante.core.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanisidro.restaurante.core.audit.model.AuditLog;
import com.sanisidro.restaurante.core.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void log(String entityName, Long entityId, String action,
                    Object oldValue, Object newValue, Long userId, String username) {
        try {
            AuditLog log = AuditLog.builder()
                    .entityName(entityName)
                    .entityId(entityId)
                    .action(action)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .userId(userId)
                    .username(username)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(log);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing audit log", e);
        }
    }
}
