package com.sanisidro.restaurante.features.cashclosing.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CashClosingReportResponse {
    private String cashierName;
    private LocalDateTime shiftStartTime;

    private BigDecimal openingBalance;

    private Map<String, BigDecimal> salesByPaymentMethod;

    private BigDecimal totalSales;

    private BigDecimal expectedCashInDrawer;
}
