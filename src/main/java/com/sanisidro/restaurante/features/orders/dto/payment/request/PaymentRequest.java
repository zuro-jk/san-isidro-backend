package com.sanisidro.restaurante.features.orders.dto.payment.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private Long orderId;
    private Long paymentMethodId;
    private BigDecimal amount;
    private Boolean isOnline;
    private String transactionCode;
}