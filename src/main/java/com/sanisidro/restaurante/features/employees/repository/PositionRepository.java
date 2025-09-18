package com.sanisidro.restaurante.features.employees.repository;

import com.sanisidro.restaurante.features.employees.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {
}
