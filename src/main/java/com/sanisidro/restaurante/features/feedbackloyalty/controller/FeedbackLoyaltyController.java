package com.sanisidro.restaurante.features.feedbackloyalty.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.feedbackloyalty.dto.response.FeedbackLoyaltySummaryResponse;
import com.sanisidro.restaurante.features.feedbackloyalty.service.FeedbackLoyaltyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/feedback-loyalty")
@RequiredArgsConstructor
public class FeedbackLoyaltyController {

    private final FeedbackLoyaltyService feedbackLoyaltyService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<FeedbackLoyaltySummaryResponse>> getFeedbackLoyaltySummary() {
        FeedbackLoyaltySummaryResponse data = feedbackLoyaltyService.getFeedbackLoyaltySummary();
        return ResponseEntity.ok(new ApiResponse<>(true, "Resumen de feedback y lealtad obtenido", data));
    }
}