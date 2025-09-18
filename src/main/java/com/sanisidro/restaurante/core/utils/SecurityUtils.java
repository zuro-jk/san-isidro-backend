package com.sanisidro.restaurante.core.utils;

import com.sanisidro.restaurante.core.security.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    /**
     * Retorna el username del usuario autenticado, o "SYSTEM" si no hay autenticación.
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "SYSTEM";
        return auth.getName();
    }

    /**
     * Retorna el ID del usuario autenticado, si está disponible.
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        }

        return null;
    }

    /**
     * Retorna el ID del usuario como Optional
     */
    public static Optional<Long> getCurrentUserIdOptional() {
        return Optional.ofNullable(getCurrentUserId());
    }
}
