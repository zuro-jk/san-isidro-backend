package com.sanisidro.restaurante.features.orders.dto.document.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {
    private Long id;
    private Long orderId;
    private String type;
    private String number;
    private BigDecimal amount;
    private LocalDateTime date;
}