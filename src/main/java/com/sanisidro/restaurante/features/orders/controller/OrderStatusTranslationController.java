package com.sanisidro.restaurante.features.orders.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.orders.dto.orderstatustranslation.request.OrderStatusTranslationRequest;
import com.sanisidro.restaurante.features.orders.dto.orderstatustranslation.response.OrderStatusTranslationResponse;
import com.sanisidro.restaurante.features.orders.service.OrderStatusTranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-status-translations")
@RequiredArgsConstructor
public class OrderStatusTranslationController {

    private final OrderStatusTranslationService service;

    @GetMapping("/order-status/{orderStatusId}")
    public ResponseEntity<ApiResponse<List<OrderStatusTranslationResponse>>> getAllByOrderStatus(
            @PathVariable Long orderStatusId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Traducciones obtenidas", service.getAllByOrderStatus(orderStatusId))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderStatusTranslationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Traducción encontrada", service.getById(id))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderStatusTranslationResponse>> create(
            @Valid @RequestBody OrderStatusTranslationRequest request) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Traducción creada", service.create(request))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderStatusTranslationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusTranslationRequest request) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Traducción actualizada", service.update(id, request))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Traducción eliminada", null));
    }
}
