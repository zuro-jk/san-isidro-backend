package com.sanisidro.restaurante.features.products.repository;

import com.sanisidro.restaurante.features.products.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
