package com.sanisidro.restaurante.features.products.dto.inventory.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private String unitName;
    private String unitSymbol;
    private BigDecimal currentStock;
    private BigDecimal minimumStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}