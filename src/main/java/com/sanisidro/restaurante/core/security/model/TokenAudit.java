package com.sanisidro.restaurante.core.security.model;

import java.time.Instant;

import com.sanisidro.restaurante.core.security.enums.TokenEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "token_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private TokenEventType eventType;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private Instant expiresAt;

    private String reason;

    private String ipAddress;

    private String userAgent;
}
