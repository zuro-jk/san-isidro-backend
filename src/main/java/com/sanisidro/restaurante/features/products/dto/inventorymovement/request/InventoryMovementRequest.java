package com.sanisidro.restaurante.features.products.dto.inventorymovement.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotBlank(message = "El tipo de movimiento es obligatorio")
    private String type;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer quantity;

    private String reason;
}
