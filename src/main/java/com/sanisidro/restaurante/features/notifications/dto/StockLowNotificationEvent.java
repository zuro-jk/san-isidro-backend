package com.sanisidro.restaurante.features.notifications.dto;

import java.math.BigDecimal;

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

    @Override
    public String getChannelKey() {
        return "WEBSOCKET";
    }
}