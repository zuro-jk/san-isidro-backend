package com.sanisidro.restaurante.core.security.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

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

    private String token;

    private String username;

    private String reason;

    private Instant blacklistedAt;

    private Instant expiresAt;

    private String ipAddress;

    private String userAgent;
}
