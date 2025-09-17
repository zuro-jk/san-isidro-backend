package com.sanisidro.restaurante.features.products.repository;

import com.sanisidro.restaurante.features.products.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    Optional<Unit> findBySymbol(String symbol);
}
