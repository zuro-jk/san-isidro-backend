package com.sanisidro.restaurante.features.products.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.products.dto.inventorymovement.request.InventoryMovementBatchRequest;
import com.sanisidro.restaurante.features.products.dto.inventorymovement.request.InventoryMovementRequest;
import com.sanisidro.restaurante.features.products.dto.inventorymovement.response.InventoryMovementResponse;
import com.sanisidro.restaurante.features.products.service.InventoryMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory-movements")
@RequiredArgsConstructor
public class InventoryMovementController {

    private final InventoryMovementService movementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryMovementResponse>>> getAll() {
        List<InventoryMovementResponse> movements = movementService.getAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Movimientos de inventario obtenidos", movements));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<InventoryMovementResponse>>> getByProduct(@PathVariable Long productId) {
        List<InventoryMovementResponse> movements = movementService.getByProduct(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Movimientos de inventario por producto obtenidos", movements));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryMovementResponse>> create(@Valid @RequestBody InventoryMovementRequest request) {
        InventoryMovementResponse movement = movementService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Movimiento de inventario creado", movement));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<InventoryMovementResponse>>> createBatch(@Valid @RequestBody InventoryMovementBatchRequest batchRequest) {
        List<InventoryMovementResponse> movements = movementService.createBatch(batchRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Movimientos de inventario creados en lote", movements));
    }
}