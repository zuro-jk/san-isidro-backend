package com.sanisidro.restaurante.features.products.dto.inventorymovement.response;

import com.sanisidro.restaurante.features.orders.enums.MovementSource;
import com.sanisidro.restaurante.features.products.enums.MovementType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementResponse {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private String unitName;
    private String unitSymbol;
    private MovementType type;
    private BigDecimal quantity;
    private LocalDateTime date;
    private String reason;
    private MovementSource source;
    private Long referenceId;
    private LocalDateTime createdAt;
}