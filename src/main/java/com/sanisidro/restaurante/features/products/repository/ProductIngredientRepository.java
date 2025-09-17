package com.sanisidro.restaurante.features.products.repository;

import com.sanisidro.restaurante.features.products.model.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Long> {
}
