package com.sanisidro.restaurante.features.customers.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanisidro.restaurante.features.customers.model.Reward;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    List<Reward> findByActiveTrue();
}
