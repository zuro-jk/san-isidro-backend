package com.sanisidro.restaurante.core.audit.listener;

import com.sanisidro.restaurante.core.audit.service.AuditLogService;
import com.sanisidro.restaurante.core.model.Auditable;
import com.sanisidro.restaurante.core.utils.SecurityUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuditableEntityListener {

    private static final Logger log = LoggerFactory.getLogger(AuditableEntityListener.class);

    private static AuditLogService auditLogService;

    @Autowired
    public void init(AuditLogService service) {
        AuditableEntityListener.auditLogService = service;
    }

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof Auditable auditable) {
            Map<String, Object> newValue = snapshotEntity(entity);
            log.debug("PrePersist snapshot: {}", newValue);
            logSafe(entity.getClass().getSimpleName(), auditable.getId(), "CREATE",
                    null, newValue, SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUsername());
            auditable.setOldSnapshot(newValue);
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof Auditable auditable) {
            Map<String, Object> oldValue = auditable.getOldSnapshot();
            Map<String, Object> newValue = snapshotEntity(entity);
            log.debug("PreUpdate old snapshot: {}", oldValue);
            log.debug("PreUpdate new snapshot: {}", newValue);
            logSafe(entity.getClass().getSimpleName(), auditable.getId(), "UPDATE",
                    oldValue, newValue, SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUsername());
            auditable.setOldSnapshot(newValue);
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        if (entity instanceof Auditable auditable) {
            Map<String, Object> oldValue = snapshotEntity(entity);
            log.debug("PreRemove snapshot: {}", oldValue);
            logSafe(entity.getClass().getSimpleName(), auditable.getId(), "DELETE",
                    oldValue, null, SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUsername());
        }
    }

    /**
     * Crea un snapshot seguro de la entidad.
     */
    public static Map<String, Object> snapshotEntity(Object entity) {
        Map<String, Object> snapshot = new HashMap<>();

        if (entity instanceof Auditable auditable) {
            snapshot.put("id", auditable.getId());
        }

        for (Field field : entity.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(entity);

                if (value == null) {
                    snapshot.put(field.getName(), null);
                    continue;
                }

                // Entidades relacionadas
                Long relatedId = getEntityId(value);
                if (relatedId != null) {
                    snapshot.put(field.getName() + "Id", relatedId);
                    continue;
                }

                // Colecciones
                if (value instanceof Collection<?> coll) {
                    // Guardar lista de IDs si son Auditable
                    var ids = coll.stream()
                            .map(AuditableEntityListener::getEntityId)
                            .collect(Collectors.toList());
                    snapshot.put(field.getName() + "Ids", ids);
                    snapshot.put(field.getName() + "Size", coll.size());
                    continue;
                }

                // Tipos simples y fechas
                if (value instanceof Number || value instanceof String || value instanceof Boolean ||
                        value instanceof LocalDate || value instanceof LocalTime || value instanceof LocalDateTime ||
                        field.getType().isEnum()) {
                    snapshot.put(field.getName(), value);
                    continue;
                }

                // Otros tipos complejos → usar toString() seguro
                try {
                    snapshot.put(field.getName(), value.toString());
                } catch (Exception e) {
                    snapshot.put(field.getName(), value.getClass().getSimpleName());
                }

            } catch (Exception e) {
                snapshot.put(field.getName(), "ERROR_ACCESSING_FIELD");
                log.error("Error accediendo al campo '{}' de {}: {}", field.getName(), entity.getClass(), e.getMessage());
            }
        }

        return snapshot;
    }

    /**
     * Obtiene ID seguro de una entidad Auditable o JPA.
     */
    private static Long getEntityId(Object entity) {
        if (entity == null) return null;

        if (entity instanceof Auditable auditable) return auditable.getId();

        if (entity instanceof HibernateProxy proxy) {
            Object impl = proxy.getHibernateLazyInitializer().getImplementation();
            if (impl instanceof Auditable real) return real.getId();
        }

        Class<?> clazz = entity.getClass();
        if (clazz.isAnnotationPresent(Entity.class)) {
            try {
                Field idField = clazz.getDeclaredField("id");
                idField.setAccessible(true);
                Object idValue = idField.get(entity);
                if (idValue instanceof Long) return (Long) idValue;
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        }

        return null;
    }

    /**
     * Loguea de forma segura para evitar errores de serialización.
     */
    private static void logSafe(String entityName, Long entityId, String action,
                                Object oldValue, Object newValue, Long userId, String username) {
        try {
            auditLogService.log(entityName, entityId, action, oldValue, newValue, userId, username);
        } catch (Exception e) {
            log.error("Error serializando log para {} id={} acción={}: {}", entityName, entityId, action, e.getMessage(), e);
            try {
                auditLogService.log(entityName, entityId, action, null,
                        Map.of("error", "SERIALIZATION_ERROR"), userId, username);
            } catch (Exception ignored) {}
        }
    }
}
