package com.sanisidro.restaurante.features.reports.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReportResponse {
    private String paymentMethodName;
    private Long totalTransactions;
    private BigDecimal totalAmount;
}