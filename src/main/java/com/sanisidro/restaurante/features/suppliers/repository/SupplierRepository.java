package com.sanisidro.restaurante.features.suppliers.repository;

import com.sanisidro.restaurante.features.suppliers.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
