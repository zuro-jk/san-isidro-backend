package com.sanisidro.restaurante.features.feedbackloyalty.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanisidro.restaurante.features.feedbackloyalty.models.LoyaltyRule;

import java.util.List;

public interface LoyaltyRuleRepository extends JpaRepository<LoyaltyRule, Long> {
    List<LoyaltyRule> findByActiveTrue();
}
