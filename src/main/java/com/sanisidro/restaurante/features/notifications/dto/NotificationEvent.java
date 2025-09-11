package com.sanisidro.restaurante.features.notifications.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {
    private Long userId;
    private String type;
    private String recipient;
    private String subject;
    private String message;
    private String logoUrl;

    private Long orderId;
    private List<OrderProduct> products;
    private BigDecimal total;
    private LocalDateTime orderDate;

    private String actionUrl;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderProduct {
        private String name;
        private BigDecimal unitPrice;
        private int quantity;
    }
}
