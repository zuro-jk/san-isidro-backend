package com.sanisidro.restaurante.features.products.dto.inventorymovement.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementBatchRequest {

    @NotEmpty(message = "Debe proporcionar al menos un movimiento")
    @Valid
    private List<InventoryMovementRequest> movements;
}