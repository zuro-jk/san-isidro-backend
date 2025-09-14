package com.sanisidro.restaurante.features.products.dto.inventorymovement.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementBatchRequest {
    private List<InventoryMovementRequest> movements;
}