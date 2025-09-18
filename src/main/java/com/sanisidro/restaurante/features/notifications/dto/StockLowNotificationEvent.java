package com.sanisidro.restaurante.features.notifications.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLowNotificationEvent implements NotifiableEvent {
    private Long userId;
    private String recipient;
    private String subject;
    private String message;
    private String actionUrl;

    private Long ingredientId;
    private String ingredientName;
    private BigDecimal currentStock;
    private BigDecimal minimumStock;
}