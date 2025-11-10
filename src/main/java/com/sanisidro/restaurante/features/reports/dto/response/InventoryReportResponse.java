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
public class InventoryReportResponse {
    private Long ingredientId;
    private String ingredientName;
    private String unitName;
    private BigDecimal currentStock;
    private BigDecimal minimumStock;
    private String status;
}