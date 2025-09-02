package com.sanisidro.restaurante.features.products.service;

import com.sanisidro.restaurante.features.products.dto.inventory.request.InventoryRequest;
import com.sanisidro.restaurante.features.products.dto.inventory.response.InventoryResponse;
import com.sanisidro.restaurante.features.products.model.Inventory;
import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.products.repository.InventoryRepository;
import com.sanisidro.restaurante.features.products.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con id: " + id));
        return mapToResponse(inventory);
    }

    public InventoryResponse create(InventoryRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + request.getProductId()));

        Inventory inventory = Inventory.builder()
                .product(product)
                .currentStock(request.getCurrentStock())
                .minimumStock(request.getMinimumStock())
                .build();

        return mapToResponse(inventoryRepository.save(inventory));
    }

    public InventoryResponse update(Long id, InventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con id: " + id));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + request.getProductId()));

        inventory.setProduct(product);
        inventory.setCurrentStock(request.getCurrentStock());
        inventory.setMinimumStock(request.getMinimumStock());

        return mapToResponse(inventoryRepository.save(inventory));
    }

    public void delete(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con id: " + id));
        inventoryRepository.delete(inventory);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .currentStock(inventory.getCurrentStock())
                .minimumStock(inventory.getMinimumStock())
                .build();
    }

}
