package com.sanisidro.restaurante.features.products.dto.inventory.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddStockRequest {
    @NotNull
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a cero")
    private BigDecimal quantity;

    private Long supplierId;
    private String reason;
}