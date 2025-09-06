package com.sanisidro.restaurante.features.products.dto.inventorymovement.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String type;
    private Integer quantity;
    private LocalDateTime date;
    private String reason;
}
