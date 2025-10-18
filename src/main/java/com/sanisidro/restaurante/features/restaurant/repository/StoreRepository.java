package com.sanisidro.restaurante.features.restaurant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanisidro.restaurante.features.restaurant.model.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByNameIgnoreCase(String name);
}
