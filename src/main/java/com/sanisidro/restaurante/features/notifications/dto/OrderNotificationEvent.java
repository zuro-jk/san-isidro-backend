package com.sanisidro.restaurante.features.notifications.dto;

import com.sanisidro.restaurante.features.notifications.templates.EmailTemplateBuilder;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotificationEvent implements NotifiableEvent {
    private Long userId;
    private String recipient;
    private String subject;
    private String message;
    private String actionUrl;

    private Long orderId;
    private List<EmailTemplateBuilder.OrderProduct> products;
    private BigDecimal total;
    private LocalDateTime orderDate;
}
