package com.sanisidro.restaurante.features.products.dto.inventory.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryCreateRequest {

    @NotNull(message = "El ID del ingrediente es obligatorio")
    private Long ingredientId;

    @NotNull(message = "El stock actual es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El stock actual no puede ser negativo")
    private BigDecimal currentStock;

    @NotNull(message = "El stock mínimo es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El stock mínimo no puede ser negativo")
    private BigDecimal minimumStock;
}