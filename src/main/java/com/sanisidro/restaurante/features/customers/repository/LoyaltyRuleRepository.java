package com.sanisidro.restaurante.features.customers.repository;

import com.sanisidro.restaurante.features.customers.model.LoyaltyRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoyaltyRuleRepository extends JpaRepository<LoyaltyRule, Long> {
    List<LoyaltyRule> findByActiveTrue();
}
