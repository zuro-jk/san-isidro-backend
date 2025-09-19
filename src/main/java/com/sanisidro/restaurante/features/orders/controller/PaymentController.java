package com.sanisidro.restaurante.features.orders.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.orders.dto.payment.request.MercadoPagoCheckoutRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.request.OnlineCheckoutRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentUpdateRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.response.PaymentResponse;
import com.sanisidro.restaurante.features.orders.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAll() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lista de pagos", paymentService.getAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Pago encontrado", paymentService.getById(id))
        );
    }

    @PostMapping("/online")
    public ResponseEntity<ApiResponse<PaymentResponse>> createOnlinePayment(
            @RequestBody @Valid OnlineCheckoutRequest request
    ) {
        PaymentResponse response = paymentService.createOnlinePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(true, "Pago creado con " + request.getProvider(), response)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> update(
            @PathVariable Long id,
            @RequestBody @Valid PaymentUpdateRequest request
    ) {
        PaymentResponse response = paymentService.update(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Pago actualizado", response)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Pago cancelado", null)
        );
    }
}