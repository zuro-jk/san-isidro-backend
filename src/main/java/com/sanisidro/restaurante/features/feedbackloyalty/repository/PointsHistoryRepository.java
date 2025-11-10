package com.sanisidro.restaurante.features.feedbackloyalty.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sanisidro.restaurante.features.feedbackloyalty.models.PointsHistory;

public interface PointsHistoryRepository extends JpaRepository<PointsHistory, Long> {
    Page<PointsHistory> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(ph.points), 0) FROM PointsHistory ph WHERE ph.event = 'EARNING'")
    int getTotalPointsAccumulated();

    @Query("SELECT COALESCE(SUM(ABS(ph.points)), 0) FROM PointsHistory ph WHERE ph.event = 'REDEMPTION'")
    int getTotalPointsRedeemed();
}
