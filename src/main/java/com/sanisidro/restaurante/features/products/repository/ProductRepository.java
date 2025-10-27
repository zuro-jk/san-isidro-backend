package com.sanisidro.restaurante.features.products.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sanisidro.restaurante.features.products.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.active = true")
    List<Product> findAllActive();

    List<Product> findByCategoryId(Long categoryId);

    Optional<Product> findByName(String name);

}