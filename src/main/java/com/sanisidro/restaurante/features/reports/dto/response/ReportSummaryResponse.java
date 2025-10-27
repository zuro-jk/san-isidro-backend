package com.sanisidro.restaurante.features.reports.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportSummaryResponse {
    private Long totalOrders; // todas las órdenes
    private BigDecimal totalSales; // ventas acumuladas

    // Hoy
    private Integer ordersToday;
    private BigDecimal salesToday;

    // Productos más vendidos
    private List<ProductSalesReportResponse> topProducts;

    // Tipos de orden
    private List<OrderTypeReportResponse> orderTypes;

    // Ventas semanales (mapa fecha -> ventas)
    private Map<LocalDate, BigDecimal> salesLast7Days;

    // Órdenes por día (opcional)
    private Map<LocalDate, Integer> ordersLast7Days;
}