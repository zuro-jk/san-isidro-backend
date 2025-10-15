package com.sanisidro.restaurante.features.products.dto.inventory.response;

import java.util.List;

import com.sanisidro.restaurante.features.products.dto.inventorymovement.response.InventoryMovementResponse;

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
public class InventoryDetailResponse {
    private InventoryResponse inventory;
    private List<InventoryMovementResponse> movements;
}