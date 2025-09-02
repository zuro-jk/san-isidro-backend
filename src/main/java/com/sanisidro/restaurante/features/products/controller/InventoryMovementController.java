package com.sanisidro.restaurante.features.products.controller;

import com.sanisidro.restaurante.features.products.dto.inventorymovement.request.InventoryMovementRequest;
import com.sanisidro.restaurante.features.products.dto.inventorymovement.response.InventoryMovementResponse;
import com.sanisidro.restaurante.features.products.service.InventoryMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-movements")
@RequiredArgsConstructor
public class InventoryMovementController {

    private final InventoryMovementService movementService;

    @GetMapping
    public ResponseEntity<List<InventoryMovementResponse>> getAll() {
        return ResponseEntity.ok(movementService.getAll());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryMovementResponse>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(movementService.getByProduct(productId));
    }

    @PostMapping
    public ResponseEntity<InventoryMovementResponse> create(@Valid @RequestBody InventoryMovementRequest request) {
        return ResponseEntity.ok(movementService.create(request));
    }
}
