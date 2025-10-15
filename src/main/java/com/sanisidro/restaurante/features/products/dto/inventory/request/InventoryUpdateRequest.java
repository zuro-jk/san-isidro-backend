package com.sanisidro.restaurante.features.products.dto.inventory.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateRequest {

    @NotNull(message = "El stock actual es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El stock actual no puede ser negativo")
    private BigDecimal currentStock;

    @NotNull(message = "El stock mínimo es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El stock mínimo no puede ser negativo")
    private BigDecimal minimumStock;
}