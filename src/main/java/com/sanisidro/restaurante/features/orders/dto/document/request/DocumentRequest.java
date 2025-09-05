package com.sanisidro.restaurante.features.orders.dto.document.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DocumentRequest {
    private Long orderId;
    private String type;
    private String number;
    private BigDecimal amount;
}