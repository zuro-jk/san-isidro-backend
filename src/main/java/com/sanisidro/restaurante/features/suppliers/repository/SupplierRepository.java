package com.sanisidro.restaurante.features.suppliers.repository;

import com.sanisidro.restaurante.features.suppliers.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByCompanyName(String companyName);
}
