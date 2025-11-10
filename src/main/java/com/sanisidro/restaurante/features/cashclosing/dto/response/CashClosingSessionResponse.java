package com.sanisidro.restaurante.features.cashclosing.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CashClosingSessionResponse {
    private Long id;
    private String employeeName;
    private LocalDateTime closingTime;
    private BigDecimal openingBalance;
    private Map<String, BigDecimal> salesByPaymentMethod;
    private BigDecimal expectedCash;
    private BigDecimal countedCash;
    private BigDecimal difference;
    private String notes;
}
