package com.sanisidro.restaurante.features.feedbackloyalty.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanisidro.restaurante.features.feedbackloyalty.models.Reward;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    List<Reward> findByActiveTrue();
}
