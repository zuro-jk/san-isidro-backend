package com.sanisidro.restaurante.features.products.dto.comboproductitem.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComboItemRequest {
    @NotNull(message = "El ID del producto en el combo no puede ser nulo")
    private Long simpleProductId;

    @Min(value = 1, message = "La cantidad del item en el combo debe ser al menos 1")
    private int quantity;
}
