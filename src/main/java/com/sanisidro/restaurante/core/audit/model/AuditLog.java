package com.sanisidro.restaurante.core.audit.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entityName;
    private Long entityId;
    private String action;

    @Lob
    private String oldValue;

    @Lob
    private String newValue;

    private Long userId;
    private String username;

    private LocalDateTime timestamp;

}
