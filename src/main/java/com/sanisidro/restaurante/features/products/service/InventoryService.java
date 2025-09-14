package com.sanisidro.restaurante.features.products.service;

import com.sanisidro.restaurante.features.products.dto.inventory.request.InventoryRequest;
import com.sanisidro.restaurante.features.products.dto.inventory.response.InventoryResponse;
import com.sanisidro.restaurante.features.products.exceptions.InventoryNotFoundException;
import com.sanisidro.restaurante.features.products.exceptions.ProductNotFoundException;
import com.sanisidro.restaurante.features.products.model.Inventory;
import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.products.repository.InventoryRepository;
import com.sanisidro.restaurante.features.products.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

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

    @Transactional
    public InventoryResponse create(InventoryRequest request) {
        validateRequest(request);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con id: " + request.getProductId()));

        Inventory inventory = Inventory.builder()
                .product(product)
                .currentStock(request.getCurrentStock())
                .minimumStock(request.getMinimumStock())
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToResponse(savedInventory);
    }

    @Transactional
    public InventoryResponse update(Long id, InventoryRequest request) {
        validateRequest(request);

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventario no encontrado con id: " + id));

        inventory.setCurrentStock(request.getCurrentStock());
        inventory.setMinimumStock(request.getMinimumStock());

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return mapToResponse(updatedInventory);
    }

    @Transactional
    public InventoryResponse partialUpdate(Long id, InventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventario no encontrado con id: " + id));

        // Solo actualizar campos no nulos en el request
        if (request.getCurrentStock() != null) {
            if (request.getCurrentStock() < 0)
                throw new IllegalArgumentException("El stock actual no puede ser negativo");
            inventory.setCurrentStock(request.getCurrentStock());
        }

        if (request.getMinimumStock() != null) {
            if (request.getMinimumStock() < 0)
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

    private void validateRequest(InventoryRequest request) {
        if (request.getCurrentStock() < 0) {
            throw new IllegalArgumentException("El stock actual no puede ser negativo");
        }
        if (request.getMinimumStock() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .currentStock(inventory.getCurrentStock())
                .minimumStock(inventory.getMinimumStock())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}