package com.sanisidro.restaurante.features.reports.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderTypeReportResponse {
    private Long orderTypeId;
    private String orderTypeName;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
}