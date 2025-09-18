package com.sanisidro.restaurante.features.suppliers.dto.purchaseorderdetail.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseOrderDetailCreateRequest {

    @NotNull(message = "El ingrediente es obligatorio")
    private Long ingredientId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer quantity;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor a 0")
    private BigDecimal unitPrice;
}