package com.sanisidro.restaurante.features.orders.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.orders.dto.paymentmethodtranslation.request.PaymentMethodTranslationRequest;
import com.sanisidro.restaurante.features.orders.dto.paymentmethodtranslation.response.PaymentMethodTranslationResponse;
import com.sanisidro.restaurante.features.orders.service.PaymentMethodTranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-method-translations")
@RequiredArgsConstructor
public class PaymentMethodTranslationController {

    private final PaymentMethodTranslationService translationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentMethodTranslationResponse>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lista de traducciones de métodos de pago", translationService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentMethodTranslationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Traducción encontrada", translationService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentMethodTranslationResponse>> create(@Valid @RequestBody PaymentMethodTranslationRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Traducción creada", translationService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentMethodTranslationResponse>> update(@PathVariable Long id,
                                                                                @Valid @RequestBody PaymentMethodTranslationRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Traducción actualizada", translationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        translationService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Traducción eliminada", null));
    }
}
