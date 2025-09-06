package com.sanisidro.restaurante.features.suppliers.repository;

import com.sanisidro.restaurante.features.suppliers.model.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Long> {
}
