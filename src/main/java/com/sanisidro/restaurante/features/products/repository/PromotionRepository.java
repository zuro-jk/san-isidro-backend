package com.sanisidro.restaurante.features.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanisidro.restaurante.features.products.model.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long>{

}
