package com.sanisidro.restaurante.features.products.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.products.dto.inventory.request.InventoryRequest;
import com.sanisidro.restaurante.features.products.dto.inventory.response.InventoryResponse;
import com.sanisidro.restaurante.features.products.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAll() {
        List<InventoryResponse> inventories = inventoryService.getAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventarios obtenidos correctamente", inventories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getById(@PathVariable Long id) {
        InventoryResponse inventory = inventoryService.getById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventario obtenido correctamente", inventory));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> create(@Valid @RequestBody InventoryRequest request) {
        InventoryResponse created = inventoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Inventario creado correctamente", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> update(@PathVariable Long id, @Valid @RequestBody InventoryRequest request) {
        InventoryResponse updated = inventoryService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventario actualizado correctamente", updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> partialUpdate(
            @PathVariable Long id,
            @RequestBody InventoryRequest request) {

        InventoryResponse updated = inventoryService.partialUpdate(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventario actualizado parcialmente", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        inventoryService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>(true, "Inventario eliminado correctamente", null));
    }
}