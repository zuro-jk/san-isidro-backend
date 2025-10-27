package com.sanisidro.restaurante.core.security.enums;

public enum TokenEventType {
    ISSUED, // Token emitido (login exitoso)
    REFRESHED, // Token renovado
    REVOKED, // Token revocado (logout, etc.)
    EXPIRED, // Token expirado
    BLACKLISTED // Token en lista negra
}
