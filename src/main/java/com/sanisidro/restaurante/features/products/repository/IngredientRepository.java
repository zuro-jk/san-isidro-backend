package com.sanisidro.restaurante.features.products.repository;

import com.sanisidro.restaurante.features.products.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByName(String name);
}