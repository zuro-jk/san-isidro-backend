package com.sanisidro.restaurante.features.feedbackloyalty.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanisidro.restaurante.features.customers.dto.review.response.ReviewResponse;
import com.sanisidro.restaurante.features.customers.service.CustomerService;
import com.sanisidro.restaurante.features.customers.service.LoyaltyService;
import com.sanisidro.restaurante.features.customers.service.ReviewService;
import com.sanisidro.restaurante.features.feedbackloyalty.dto.response.FeedbackLoyaltySummaryResponse;
import com.sanisidro.restaurante.features.feedbackloyalty.dto.response.LoyalCustomerResponse;
import com.sanisidro.restaurante.features.feedbackloyalty.dto.response.RewardResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackLoyaltyService {

    private final ReviewService reviewService;
    private final CustomerService customerService;
    private final LoyaltyService loyaltyService;

    public FeedbackLoyaltySummaryResponse getFeedbackLoyaltySummary() {
        List<ReviewResponse> recentReviews = reviewService.findRecentReviews(5);
        double averageRating = reviewService.calculateAverageSatisfaction();

        long totalRegisteredCustomers = customerService.countAllCustomers();
        int totalPointsAccumulated = loyaltyService.getTotalPointsAccumulated();
        int totalPointsRedeemed = loyaltyService.getTotalPointsRedeemed();

        // Clientes más leales y premios
        List<LoyalCustomerResponse> topLoyalCustomers = loyaltyService.getTopLoyalCustomers(3);
        List<RewardResponse> availableRewards = loyaltyService.getAvailableRewards();

        return FeedbackLoyaltySummaryResponse.builder()
                .recentReviews(recentReviews)
                .averageRating(averageRating)
                .totalRegisteredCustomers(totalRegisteredCustomers)
                .totalPointsAccumulated(totalPointsAccumulated)
                .totalPointsRedeemed(totalPointsRedeemed)
                .topLoyalCustomers(topLoyalCustomers)
                .availableRewards(availableRewards)
                .build();
    }

}
