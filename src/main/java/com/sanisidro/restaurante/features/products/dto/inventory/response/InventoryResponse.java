package com.sanisidro.restaurante.features.products.dto.inventory.response;

import lombok.*;

import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}