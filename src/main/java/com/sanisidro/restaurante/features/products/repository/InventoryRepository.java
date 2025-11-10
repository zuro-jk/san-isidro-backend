package com.sanisidro.restaurante.features.products.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sanisidro.restaurante.features.products.model.Ingredient;
import com.sanisidro.restaurante.features.products.model.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByIngredientId(Long ingredientId);

    Optional<Inventory> findByIngredient(Ingredient ingredient);

    @Query("""
                SELECT i FROM Inventory i
                WHERE i.currentStock <= i.minimumStock
                ORDER BY i.currentStock ASC
            """)
    List<Inventory> findLowStockInventories();

    /**
     * Obtiene una vista completa del inventario actual,
     * uniendo Ingrediente y Unidad para un reporte completo.
     */
    @Query("SELECT i.ingredient.id, i.ingredient.name, i.ingredient.unit.name, i.currentStock, i.minimumStock " +
            "FROM Inventory i " +
            "JOIN i.ingredient " +
            "JOIN i.ingredient.unit " +
            "ORDER BY i.ingredient.name ASC")
    List<Object[]> findInventoryReport();
}
