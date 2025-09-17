package com.sanisidro.restaurante.features.products.dto.ingredient.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IngredientRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "La unidad es obligatoria")
    private Long unitId;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    private Integer minStock;
}
