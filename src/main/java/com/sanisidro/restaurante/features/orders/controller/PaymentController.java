package com.sanisidro.restaurante.features.orders.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.orders.dto.payment.request.OnlineCheckoutRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentUpdateRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.response.PaymentResponse;
import com.sanisidro.restaurante.features.orders.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

        private final PaymentService paymentService;

        @GetMapping
        public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAll(
                        @RequestParam(required = false) Long customerId,
                        @RequestParam(required = false) Long paymentMethodId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {
                List<PaymentResponse> payments = paymentService.getAll(
                                customerId, paymentMethodId, dateFrom, dateTo);
                return ResponseEntity.ok(
                                new ApiResponse<>(true, "Lista de pagos", payments));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable Long id) {
                return ResponseEntity.ok(
                                new ApiResponse<>(true, "Pago encontrado", paymentService.getById(id)));
        }

        @PostMapping("/online")
        public ResponseEntity<ApiResponse<PaymentResponse>> createOnlinePayment(
                        @RequestBody @Valid OnlineCheckoutRequest request) throws Exception {

                PaymentResponse response = paymentService.createOnlinePayment(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                new ApiResponse<>(true, "Pago creado con MercadoPago", response));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<PaymentResponse>> update(
                        @PathVariable Long id,
                        @RequestBody @Valid PaymentUpdateRequest request) {
                PaymentResponse response = paymentService.update(id, request);
                return ResponseEntity.ok(
                                new ApiResponse<>(true, "Pago actualizado", response));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
                paymentService.delete(id);
                return ResponseEntity.ok(
                                new ApiResponse<>(true, "Pago cancelado", null));
        }
}