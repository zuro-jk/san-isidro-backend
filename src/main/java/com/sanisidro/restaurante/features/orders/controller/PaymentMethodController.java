package com.sanisidro.restaurante.features.orders.controller;


import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.orders.dto.paymentmethod.request.PaymentMethodRequest;
import com.sanisidro.restaurante.features.orders.dto.paymentmethod.resposne.PaymentMethodResponse;
import com.sanisidro.restaurante.features.orders.service.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lista de métodos de pago", paymentMethodService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Método de pago encontrado", paymentMethodService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> create(@Valid @RequestBody PaymentMethodRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Método de pago creado", paymentMethodService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> update(@PathVariable Long id,
                                                                     @Valid @RequestBody PaymentMethodRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Método de pago actualizado", paymentMethodService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        paymentMethodService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Método de pago eliminado", null));
    }

}
