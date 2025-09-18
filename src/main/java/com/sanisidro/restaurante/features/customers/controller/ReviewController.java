package com.sanisidro.restaurante.features.customers.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.customers.dto.review.request.ReviewRequest;
import com.sanisidro.restaurante.features.customers.dto.review.response.ReviewResponse;
import com.sanisidro.restaurante.features.customers.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

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
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@Valid @RequestBody ReviewRequest dto) {
        ReviewResponse review = reviewService.createReview(dto);
        return new ResponseEntity<>(new ApiResponse<>(true, "Reseña creada correctamente", review), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest dto) {
        ReviewResponse review = reviewService.updateReview(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reseña actualizada correctamente", review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reseña eliminada correctamente", null));
    }

}
