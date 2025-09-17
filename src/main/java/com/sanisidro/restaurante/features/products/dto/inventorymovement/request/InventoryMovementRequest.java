package com.sanisidro.restaurante.features.products.dto.inventorymovement.request;

import com.sanisidro.restaurante.features.orders.enums.MovementSource;
import com.sanisidro.restaurante.features.products.enums.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementRequest {

    @NotNull(message = "El ID del ingrediente es obligatorio")
    private Long ingredientId;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private MovementType type;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer quantity;

    private String reason;

    private LocalDateTime date;

    @NotNull(message = "El origen del movimiento es obligatorio")
    private MovementSource source;

    private Long referenceId;
}