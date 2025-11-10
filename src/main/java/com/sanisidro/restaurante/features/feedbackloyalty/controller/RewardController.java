package com.sanisidro.restaurante.features.feedbackloyalty.controller;

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
import com.sanisidro.restaurante.features.feedbackloyalty.dto.reward.request.RewardRequest;
import com.sanisidro.restaurante.features.feedbackloyalty.dto.reward.response.RewardResponse;
import com.sanisidro.restaurante.features.feedbackloyalty.service.RewardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<RewardResponse>>> getActiveRewards() {
        List<RewardResponse> rewards = rewardService.getActiveRewards();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Recompensas activas obtenidas", rewards));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<RewardResponse>>> getAllRewards() {
        List<RewardResponse> rewards = rewardService.getAllRewards();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Todas las recompensas obtenidas", rewards));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RewardResponse>> getRewardById(@PathVariable Long id) {
        RewardResponse reward = rewardService.getRewardById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Recompensa encontrada", reward));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RewardResponse>> createReward(
            @Valid @RequestBody RewardRequest request) {
        RewardResponse newReward = rewardService.createReward(request);
        return ResponseEntity.status(201).body(
                new ApiResponse<>(true, "Recompensa creada exitosamente", newReward));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RewardResponse>> updateReward(
            @PathVariable Long id,
            @Valid @RequestBody RewardRequest request) {
        RewardResponse updatedReward = rewardService.updateReward(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Recompensa actualizada exitosamente", updatedReward));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReward(@PathVariable Long id) {
        rewardService.deleteReward(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Recompensa eliminada exitosamente", null));
    }

}
