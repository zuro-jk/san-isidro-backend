package com.sanisidro.restaurante.features.products.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.products.dto.inventory.request.AddStockRequest;
import com.sanisidro.restaurante.features.products.dto.inventory.request.InventoryCreateRequest;
import com.sanisidro.restaurante.features.products.dto.inventory.request.InventoryUpdateRequest;
import com.sanisidro.restaurante.features.products.dto.inventory.response.InventoryDetailResponse;
import com.sanisidro.restaurante.features.products.dto.inventory.response.InventoryResponse;
import com.sanisidro.restaurante.features.products.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<ApiResponse<InventoryDetailResponse>> getById(@PathVariable Long id) {
        InventoryDetailResponse inventoryDetail = inventoryService.getById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventario obtenido correctamente", inventoryDetail));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> create(@Valid @RequestBody InventoryCreateRequest request) {
        InventoryResponse created = inventoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Inventario creado correctamente", created));
    }

    @PostMapping("/{id}/add-stock")
    public ResponseEntity<ApiResponse<InventoryResponse>> addStock(
            @PathVariable Long id,
            @Valid @RequestBody AddStockRequest request) {
        InventoryResponse updated = inventoryService.addStock(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock actualizado correctamente", updated));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> update(@PathVariable Long id,
            @Valid @RequestBody InventoryUpdateRequest request) {
        InventoryResponse updated = inventoryService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventario actualizado correctamente", updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> partialUpdate(
            @PathVariable Long id,
            @RequestBody InventoryCreateRequest request) {
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