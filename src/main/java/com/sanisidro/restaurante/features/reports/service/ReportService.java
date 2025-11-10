package com.sanisidro.restaurante.features.reports.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sanisidro.restaurante.features.orders.repository.PaymentRepository;
import com.sanisidro.restaurante.features.orders.service.OrderService;
import com.sanisidro.restaurante.features.products.repository.InventoryRepository;
import com.sanisidro.restaurante.features.products.service.ProductService;
import com.sanisidro.restaurante.features.reports.dto.response.InventoryReportResponse;
import com.sanisidro.restaurante.features.reports.dto.response.OrderTypeReportResponse;
import com.sanisidro.restaurante.features.reports.dto.response.PaymentReportResponse;
import com.sanisidro.restaurante.features.reports.dto.response.ProductSalesReportResponse;
import com.sanisidro.restaurante.features.reports.dto.response.ReportSummaryResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final OrderService orderService;
    private final ProductService productService;

    private final PaymentRepository paymentRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Construye y devuelve un resumen completo de reportes.
     * Implementación concreta depende de los métodos expuestos por OrderService /
     * ProductService.
     */
    public ReportSummaryResponse getFullReportSummary() {
        // Totales generales
        Long totalOrders = orderService.countAllOrders();
        BigDecimal totalSales = orderService.calculateTotalSales();

        // Hoy
        LocalDate today = LocalDate.now();
        Integer ordersToday = orderService.countOrdersByDate(today);
        BigDecimal salesToday = orderService.calculateSalesByDate(today);

        // Productos más vendidos (top N)
        List<ProductSalesReportResponse> topProducts = productService.getTopSellingProducts();

        // Tipos de orden
        List<OrderTypeReportResponse> orderTypes = orderService.getOrderTypeStatistics("es");

        // Ventas y órdenes últimos 7 días
        Map<LocalDate, BigDecimal> salesLast7Days = orderService.calculateSalesLast7Days();
        Map<LocalDate, Integer> ordersLast7Days = orderService.countOrdersLast7Days();

        return new ReportSummaryResponse(
                totalOrders,
                totalSales,
                ordersToday,
                salesToday,
                topProducts,
                orderTypes,
                salesLast7Days,
                ordersLast7Days);
    }

    public List<PaymentReportResponse> getPaymentMethodSummary() {
        return paymentRepository.findPaymentSummaryByMethod().stream()
                .map(row -> PaymentReportResponse.builder()
                        .paymentMethodName((String) row[0])
                        .totalTransactions((Long) row[1])
                        .totalAmount((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }

    public List<InventoryReportResponse> getInventoryReport() {
        return inventoryRepository.findInventoryReport().stream()
                .map(row -> {
                    BigDecimal currentStock = (BigDecimal) row[3];
                    BigDecimal minimumStock = (BigDecimal) row[4];
                    String status = "ÓPTIMO";

                    if (currentStock.compareTo(minimumStock) <= 0) {
                        status = "CRÍTICO";
                    } else if (currentStock.compareTo(minimumStock.multiply(new BigDecimal("1.5"))) <= 0) {
                        status = "BAJO";
                    }

                    return InventoryReportResponse.builder()
                            .ingredientId((Long) row[0])
                            .ingredientName((String) row[1])
                            .unitName((String) row[2])
                            .currentStock(currentStock)
                            .minimumStock(minimumStock)
                            .status(status)
                            .build();
                })
                .collect(Collectors.toList());
    }

}
