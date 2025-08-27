package com.sanisidro.restaurante.core.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChanguePasswordRequest {
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String currentPassword;
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String newPassword;
}
