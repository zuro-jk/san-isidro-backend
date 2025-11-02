package com.sanisidro.restaurante.features.customers.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.features.customers.dto.review.request.ReviewRequest;
import com.sanisidro.restaurante.features.customers.dto.review.response.ReviewResponse;
import com.sanisidro.restaurante.features.customers.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReview(@PathVariable Long id) {
        ReviewResponse review = reviewService.getReview(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reseña obtenida correctamente", review));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByCustomer(@PathVariable Long customerId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByCustomer(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reseñas del cliente obtenidas correctamente", reviews));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@Valid @RequestBody ReviewRequest dto,
            @AuthenticationPrincipal User user) {
        ReviewResponse review = reviewService.createReview(dto, user);
        return new ResponseEntity<>(new ApiResponse<>(true, "Reseña creada correctamente", review), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest dto,
            @AuthenticationPrincipal User user) {
        ReviewResponse review = reviewService.updateReview(id, dto, user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reseña actualizada correctamente", review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id, @AuthenticationPrincipal User user) {
        reviewService.deleteReview(id, user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reseña eliminada correctamente", null));
    }

}
