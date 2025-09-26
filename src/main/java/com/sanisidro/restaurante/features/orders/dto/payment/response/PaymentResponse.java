package com.sanisidro.restaurante.features.orders.dto.payment.response;

import com.sanisidro.restaurante.features.orders.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Long paymentMethodId;
    private String paymentMethodName;
    private BigDecimal amount;
    private LocalDateTime date;
    private Boolean isOnline;
    private String transactionCode;
    private String provider;
    private PaymentStatus status;
}