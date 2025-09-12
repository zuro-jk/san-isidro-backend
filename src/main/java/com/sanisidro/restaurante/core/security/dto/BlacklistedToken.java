package com.sanisidro.restaurante.core.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken implements Serializable {
    private String token;
    private String username;
    private Instant revokedAt;
    private String reason;
}
