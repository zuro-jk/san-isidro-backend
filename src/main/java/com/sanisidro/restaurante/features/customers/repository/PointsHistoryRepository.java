package com.sanisidro.restaurante.features.customers.repository;

import com.sanisidro.restaurante.features.customers.model.PointsHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointsHistoryRepository extends JpaRepository<PointsHistory, Long> {
    Page<PointsHistory> findByCustomerId(Long customerId, Pageable pageable);
}
