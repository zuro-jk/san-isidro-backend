package com.sanisidro.restaurante.features.products.service;

import com.sanisidro.restaurante.features.products.dto.inventory.request.InventoryCreateRequest;
import com.sanisidro.restaurante.features.products.dto.inventory.request.InventoryUpdateRequest;
import com.sanisidro.restaurante.features.products.dto.inventory.response.InventoryResponse;
import com.sanisidro.restaurante.features.products.exceptions.IngredientNotFoundException;
import com.sanisidro.restaurante.features.products.exceptions.InventoryAlreadyExistsException;
import com.sanisidro.restaurante.features.products.exceptions.InventoryNotFoundException;
import com.sanisidro.restaurante.features.products.model.Ingredient;
import com.sanisidro.restaurante.features.products.model.Inventory;
import com.sanisidro.restaurante.features.products.repository.IngredientRepository;
import com.sanisidro.restaurante.features.products.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final IngredientRepository ingredientRepository;

    public List<InventoryResponse> getAll() {
        return inventoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public InventoryResponse getById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventario no encontrado con id: " + id));
        return mapToResponse(inventory);
    }

    public InventoryResponse getByIngredient(Long ingredientId) {
        Inventory inventory = inventoryRepository.findByIngredientId(ingredientId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventario no encontrado para el ingrediente id: " + ingredientId));
        return mapToResponse(inventory);
    }

    @Transactional
    public InventoryResponse create(InventoryCreateRequest request) {
        validateRequest(request);

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new IngredientNotFoundException(
                        "Ingrediente no encontrado con id: " + request.getIngredientId()
                ));

        inventoryRepository.findByIngredientId(request.getIngredientId())
                .ifPresent(inv -> {
                    throw new InventoryAlreadyExistsException(
                            "Ya existe un inventario para el ingrediente con id: " + request.getIngredientId()
                    );
                });

        Inventory inventory = Inventory.builder()
                .ingredient(ingredient)
                .currentStock(request.getCurrentStock())
                .minimumStock(request.getMinimumStock())
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToResponse(savedInventory);
    }

    @Transactional
    public InventoryResponse update(Long id, InventoryUpdateRequest request) {
        validateRequest(request);

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventario no encontrado con id: " + id));

        inventory.setCurrentStock(request.getCurrentStock());
        inventory.setMinimumStock(request.getMinimumStock());

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return mapToResponse(updatedInventory);
    }

    @Transactional
    public InventoryResponse partialUpdate(Long id, InventoryCreateRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventario no encontrado con id: " + id));

        if (request.getCurrentStock() != null) {
            if (request.getCurrentStock().compareTo(BigDecimal.ZERO) < 0)
                throw new IllegalArgumentException("El stock actual no puede ser negativo");
            inventory.setCurrentStock(request.getCurrentStock());
        }

        if (request.getMinimumStock() != null) {
            if (request.getMinimumStock().compareTo(BigDecimal.ZERO) < 0)
                throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
            inventory.setMinimumStock(request.getMinimumStock());
        }

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return mapToResponse(updatedInventory);
    }

    public void delete(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventario no encontrado con id: " + id));
        inventoryRepository.delete(inventory);
    }

    private void validateRequest(InventoryCreateRequest request) {
        validateStock(request.getCurrentStock(), request.getMinimumStock());
    }

    private void validateRequest(InventoryUpdateRequest request) {
        validateStock(request.getCurrentStock(), request.getMinimumStock());
    }

    private void validateStock(BigDecimal currentStock, BigDecimal minimumStock) {
        if (currentStock != null && currentStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El stock actual no puede ser negativo");
        }
        if (minimumStock != null && minimumStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        Ingredient ingredient = inventory.getIngredient();

        return InventoryResponse.builder()
                .id(inventory.getId())
                .ingredientId(ingredient.getId())
                .ingredientName(ingredient.getName())
                .unitName(ingredient.getUnit().getName())
                .unitSymbol(ingredient.getUnit().getSymbol())
                .currentStock(inventory.getCurrentStock())
                .minimumStock(inventory.getMinimumStock())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}