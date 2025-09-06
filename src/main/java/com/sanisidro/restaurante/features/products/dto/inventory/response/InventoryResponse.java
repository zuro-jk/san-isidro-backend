package com.sanisidro.restaurante.features.products.dto.inventory.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer minimumStock;
}
