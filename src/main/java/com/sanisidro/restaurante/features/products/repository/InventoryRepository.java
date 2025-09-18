package com.sanisidro.restaurante.features.products.repository;

import com.sanisidro.restaurante.features.products.model.Ingredient;
import com.sanisidro.restaurante.features.products.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByIngredientId(Long ingredientId);
    Optional<Inventory> findByIngredient(Ingredient ingredient);
}
