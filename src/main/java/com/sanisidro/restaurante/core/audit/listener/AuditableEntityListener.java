package com.sanisidro.restaurante.core.audit.listener;

import com.sanisidro.restaurante.core.audit.service.AuditLogService;
import com.sanisidro.restaurante.core.model.Auditable;
import com.sanisidro.restaurante.core.utils.SecurityUtils;
import com.sanisidro.restaurante.features.customers.model.Customer;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuditableEntityListener {

    private static AuditLogService auditLogService;

    @Autowired
    public void init(AuditLogService auditLogService) {
        AuditableEntityListener.auditLogService = auditLogService;
    }

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof Auditable auditable) {
            Map<String, Object> newValue = snapshotEntity(entity);

            auditLogService.log(
                    entity.getClass().getSimpleName(),
                    null,
                    "CREATE",
                    null,
                    newValue,
                    SecurityUtils.getCurrentUserId(),
                    SecurityUtils.getCurrentUsername()
            );

            // Guardamos el snapshot inicial para futuras actualizaciones
            auditable.setOldSnapshot(newValue);
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof Auditable auditable) {
            Map<String, Object> oldValue = auditable.getOldSnapshot(); // snapshot anterior
            Map<String, Object> newValue = snapshotEntity(entity);

            auditLogService.log(
                    entity.getClass().getSimpleName(),
                    auditable.getId(),
                    "UPDATE",
                    oldValue,
                    newValue,
                    SecurityUtils.getCurrentUserId(),
                    SecurityUtils.getCurrentUsername()
            );

            // Actualizamos snapshot para la próxima vez
            auditable.setOldSnapshot(newValue);
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        if (entity instanceof Auditable auditable) {
            Map<String, Object> oldValue = snapshotEntity(entity);

            auditLogService.log(
                    entity.getClass().getSimpleName(),
                    auditable.getId(),
                    "DELETE",
                    oldValue,
                    null,
                    SecurityUtils.getCurrentUserId(),
                    SecurityUtils.getCurrentUsername()
            );
        }
    }

    /**
     * Toma los campos importantes de la entidad para auditar.
     * Agrega más entidades según necesites.
     */
    private Map<String, Object> snapshotEntity(Object entity) {
        Map<String, Object> snapshot = new HashMap<>();

        if (entity instanceof Customer customer) {
            snapshot.put("id", customer.getId());
            snapshot.put("userId", customer.getUser().getId());
            snapshot.put("points", customer.getPoints());
        }

        // Aquí podrías agregar más entidades con sus campos importantes
        return snapshot;
    }
}
