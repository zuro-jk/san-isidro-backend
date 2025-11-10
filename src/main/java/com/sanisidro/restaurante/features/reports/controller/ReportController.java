package com.sanisidro.restaurante.features.reports.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.reports.dto.response.InventoryReportResponse;
import com.sanisidro.restaurante.features.reports.dto.response.PaymentReportResponse;
import com.sanisidro.restaurante.features.reports.dto.response.ReportSummaryResponse;
import com.sanisidro.restaurante.features.reports.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Devuelve un resumen con todos los reportes necesarios para el frontend:
     * totales, hoy, top products, tipos de orden y ventas semanales.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> getReportSummary() {
        ReportSummaryResponse resp = reportService.getFullReportSummary();
        return ResponseEntity.ok(new ApiResponse<>(true, "Resumen de reportes generado", resp));
    }

    /**
     * Devuelve un resumen de transacciones por método de pago.
     */
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<PaymentReportResponse>>> getPaymentsReport() {
        List<PaymentReportResponse> resp = reportService.getPaymentMethodSummary();
        return ResponseEntity.ok(new ApiResponse<>(true, "Reporte de pagos generado", resp));
    }

    /**
     * Devuelve el estado actual del inventario y alertas de reposición.
     */
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<InventoryReportResponse>>> getInventoryReport() {
        List<InventoryReportResponse> resp = reportService.getInventoryReport();
        return ResponseEntity.ok(new ApiResponse<>(true, "Reporte de inventario generado", resp));
    }

}
