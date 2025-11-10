package com.sanisidro.restaurante.features.feedbackloyalty.dto.reward.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RewardRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;

    @NotNull(message = "Los puntos requeridos son obligatorios")
    @Min(value = 1, message = "Se requiere al menos 1 punto")
    private Integer requiredPoints;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean active;
}
