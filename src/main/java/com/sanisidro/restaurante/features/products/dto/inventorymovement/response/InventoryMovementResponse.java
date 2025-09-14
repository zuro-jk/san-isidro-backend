package com.sanisidro.restaurante.features.products.dto.inventorymovement.response;

import com.sanisidro.restaurante.features.orders.enums.MovementSource;
import com.sanisidro.restaurante.features.products.enums.MovementType;
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
    private MovementType type;
    private Integer quantity;
    private LocalDateTime date;
    private String reason;
    private MovementSource source;
    private Long referenceId;
    private LocalDateTime createdAt;
}