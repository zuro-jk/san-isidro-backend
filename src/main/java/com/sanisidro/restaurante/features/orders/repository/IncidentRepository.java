package com.sanisidro.restaurante.features.orders.repository;

import com.sanisidro.restaurante.features.orders.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
}
