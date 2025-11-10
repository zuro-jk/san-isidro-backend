package com.sanisidro.restaurante.features.cashclosing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.features.cashclosing.dto.request.CashClosingSubmitRequest;
import com.sanisidro.restaurante.features.cashclosing.dto.response.CashClosingReportResponse;
import com.sanisidro.restaurante.features.cashclosing.dto.response.CashClosingSessionResponse;
import com.sanisidro.restaurante.features.cashclosing.services.CashClosingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cash-closing")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_CASHIER', 'ROLE_ADMIN')")
public class CashClosingController {

    private final CashClosingService cashClosingService;

    /**
     * Endpoint para OBTENER el reporte del sistema para el cajero actual.
     */
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<CashClosingReportResponse>> getCurrentReport(
            @AuthenticationPrincipal User cashierUser) {

        CashClosingReportResponse report = cashClosingService.generateCurrentReport(cashierUser);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Reporte de caja obtenido correctamente", report));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<CashClosingSessionResponse>>> getHistory() {

        List<CashClosingSessionResponse> history = cashClosingService.getAllClosingSessions();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Historial de cierres de caja obtenido", history));
    }

    /**
     * Endpoint para ENVIAR y GUARDAR el cierre de caja.
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<Void>> submitCashClosing(
            @AuthenticationPrincipal User cashierUser,
            @Valid @RequestBody CashClosingSubmitRequest request) {

        cashClosingService.submitCashClosing(cashierUser, request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Cierre de caja guardado exitosamente", null));
    }

}
