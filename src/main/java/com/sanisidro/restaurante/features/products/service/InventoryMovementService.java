package com.sanisidro.restaurante.features.products.service;

import com.sanisidro.restaurante.features.products.dto.inventorymovement.request.InventoryMovementBatchRequest;
import com.sanisidro.restaurante.features.products.dto.inventorymovement.request.InventoryMovementRequest;
import com.sanisidro.restaurante.features.products.dto.inventorymovement.response.InventoryMovementResponse;
import com.sanisidro.restaurante.features.products.enums.MovementType;
import com.sanisidro.restaurante.features.products.exceptions.*;
import com.sanisidro.restaurante.features.products.model.Inventory;
import com.sanisidro.restaurante.features.products.model.InventoryMovement;
import com.sanisidro.restaurante.features.products.model.Ingredient;
import com.sanisidro.restaurante.features.products.repository.InventoryMovementRepository;
import com.sanisidro.restaurante.features.products.repository.InventoryRepository;
import com.sanisidro.restaurante.features.products.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final InventoryRepository inventoryRepository;
    private final IngredientRepository ingredientRepository;

    public List<InventoryMovementResponse> getAll() {
        return movementRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<InventoryMovementResponse> getByIngredient(Long ingredientId) {
        ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new IngredientNotFoundException("Ingrediente no encontrado con id: " + ingredientId));

        return movementRepository.findByIngredientId(ingredientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public InventoryMovementResponse create(InventoryMovementRequest request) {
        List<InventoryMovementResponse> results = createBatch(new InventoryMovementBatchRequest(List.of(request)));
        return results.get(0);
    }

    @Transactional
    public List<InventoryMovementResponse> createBatch(InventoryMovementBatchRequest batchRequest) {
        List<InventoryMovementResponse> responses = new ArrayList<>();

        for (InventoryMovementRequest request : batchRequest.getMovements()) {
            validateRequest(request);

            Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                    .orElseThrow(() -> new IngredientNotFoundException("Ingrediente no encontrado con id: " + request.getIngredientId()));

            Inventory inventory = inventoryRepository.findByIngredientId(request.getIngredientId())
                    .orElseThrow(() -> new InventoryNotFoundException("Inventario no encontrado para el ingrediente id: " + request.getIngredientId()));

            applyStockChange(inventory, request);

            inventoryRepository.flush();

            InventoryMovement movement = InventoryMovement.builder()
                    .ingredient(ingredient)
                    .type(request.getType())
                    .quantity(request.getQuantity())
                    .reason(request.getReason())
                    .date(request.getDate())
                    .source(request.getSource())
                    .referenceId(request.getReferenceId())
                    .build();

            InventoryMovement savedMovement = movementRepository.save(movement);
            responses.add(mapToResponse(savedMovement));
        }

        return responses;
    }

    private void validateRequest(InventoryMovementRequest request) {
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuantityException("La cantidad debe ser mayor a cero para ingredienteId: " + request.getIngredientId());
        }
        if (request.getType() == null) {
            throw new InvalidMovementTypeException("Tipo de movimiento es obligatorio para ingredienteId: " + request.getIngredientId());
        }
        if (request.getSource() == null) {
            throw new IllegalArgumentException("El origen del movimiento es obligatorio para ingredienteId: " + request.getIngredientId());
        }
    }

    private void applyStockChange(Inventory inventory, InventoryMovementRequest request) {
        BigDecimal qty = request.getQuantity();
        MovementType type = request.getType();

        switch (type) {
            case IN -> inventory.increaseStock(qty);
            case OUT -> inventory.decreaseStock(qty);
            default -> throw new InvalidMovementTypeException("Tipo de movimiento no válido para ingredienteId: " + request.getIngredientId());
        }
    }

    private InventoryMovementResponse mapToResponse(InventoryMovement movement) {
        return InventoryMovementResponse.builder()
                .id(movement.getId())
                .ingredientId(movement.getIngredient().getId())
                .ingredientName(movement.getIngredient().getName())
                .unitName(movement.getIngredient().getUnit().getName())
                .unitSymbol(movement.getIngredient().getUnit().getSymbol())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .date(movement.getDate())
                .reason(movement.getReason())
                .source(movement.getSource())
                .referenceId(movement.getReferenceId())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
