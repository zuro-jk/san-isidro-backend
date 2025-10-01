package com.sanisidro.restaurante.core.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChanguePasswordRequest {
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String newPassword;
}