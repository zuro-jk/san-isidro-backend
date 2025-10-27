package com.sanisidro.restaurante.features.reports.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SalesReportResponse {
    private LocalDate date;
    private BigDecimal totalSales;
    private int totalOrders;
}