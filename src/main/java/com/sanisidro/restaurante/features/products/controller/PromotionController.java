package com.sanisidro.restaurante.features.products.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.products.dto.promotion.request.CreatePromotionRequest;
import com.sanisidro.restaurante.features.products.dto.promotion.request.UpdatePromotionRequest;
import com.sanisidro.restaurante.features.products.dto.promotion.response.PromotionResponse;
import com.sanisidro.restaurante.features.products.service.PromotionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @PreAuthorize("permitAll")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getAllPromotions() {
        List<PromotionResponse> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Promociones obtenidas", promotions));
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll")
    public ResponseEntity<ApiResponse<PromotionResponse>> getPromotionById(@PathVariable Long id) {
        PromotionResponse promotion = promotionService.getPromotionById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Promoción encontrada", promotion));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
            @Valid @RequestBody CreatePromotionRequest request) {
        PromotionResponse newPromotion = promotionService.createPromotion(request);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(true, "Promoción creada exitosamente", newPromotion));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePromotionRequest request) {
        PromotionResponse updatedPromotion = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Promoción actualizada exitosamente", updatedPromotion));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Promoción eliminada exitosamente", null));
    }
}
