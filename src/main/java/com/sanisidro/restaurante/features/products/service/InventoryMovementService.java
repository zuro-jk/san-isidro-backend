package com.sanisidro.restaurante.features.products.service;

import com.sanisidro.restaurante.features.products.dto.inventorymovement.request.InventoryMovementRequest;
import com.sanisidro.restaurante.features.products.dto.inventorymovement.response.InventoryMovementResponse;
import com.sanisidro.restaurante.features.products.model.Inventory;
import com.sanisidro.restaurante.features.products.model.InventoryMovement;
import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.products.repository.InventoryMovementRepository;
import com.sanisidro.restaurante.features.products.repository.InventoryRepository;
import com.sanisidro.restaurante.features.products.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public List<InventoryMovementResponse> getAll() {
        return movementRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<InventoryMovementResponse> getByProduct(Long productId) {
        return movementRepository.findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public InventoryMovementResponse create(InventoryMovementRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + request.getProductId()));

        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado para el producto id: " + request.getProductId()));

        // Validar y aplicar movimiento
        if ("ENTRADA".equalsIgnoreCase(request.getType())) {
            inventory.setCurrentStock(inventory.getCurrentStock() + request.getQuantity());
        } else if ("SALIDA".equalsIgnoreCase(request.getType())) {
            if (inventory.getCurrentStock() < request.getQuantity()) {
                throw new IllegalArgumentException("Stock insuficiente para realizar la salida");
            }
            inventory.setCurrentStock(inventory.getCurrentStock() - request.getQuantity());
        } else {
            throw new IllegalArgumentException("Tipo de movimiento inválido: " + request.getType());
        }

        // Guardar inventario actualizado
        inventoryRepository.save(inventory);

        // Registrar movimiento
        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .type(request.getType().toUpperCase())
                .quantity(request.getQuantity())
                .date(LocalDateTime.now())
                .reason(request.getReason())
                .build();

        return mapToResponse(movementRepository.save(movement));
    }

    private InventoryMovementResponse mapToResponse(InventoryMovement movement) {
        return InventoryMovementResponse.builder()
                .id(movement.getId())
                .productId(movement.getProduct().getId())
                .productName(movement.getProduct().getName())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .date(movement.getDate())
                .reason(movement.getReason())
                .build();
    }
}
