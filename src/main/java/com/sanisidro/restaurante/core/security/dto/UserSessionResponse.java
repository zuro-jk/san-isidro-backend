package com.sanisidro.restaurante.core.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class UserSessionResponse {
    private String sessionId;
    private Instant expiryDate;
    private String ip;
    private String userAgent;
}
