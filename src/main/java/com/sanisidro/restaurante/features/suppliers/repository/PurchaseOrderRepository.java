package com.sanisidro.restaurante.features.suppliers.repository;

import com.sanisidro.restaurante.features.suppliers.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
}
