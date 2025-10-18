package com.sanisidro.restaurante.features.restaurant.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.restaurant.dto.store.request.StoreRequest;
import com.sanisidro.restaurante.features.restaurant.dto.store.response.StoreResponse;
import com.sanisidro.restaurante.features.restaurant.service.StoreService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@Valid @RequestBody StoreRequest request) {
        StoreResponse createdStore = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Sucursal creada correctamente", createdStore));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getAllStores() {
        List<StoreResponse> stores = storeService.getAllStores();
        return ResponseEntity.ok(new ApiResponse<>(true, "Sucursales obtenidas correctamente", stores));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable Long id) {
        StoreResponse store = storeService.getStoreById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sucursal obtenida correctamente", store));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(@PathVariable Long id,
            @Valid @RequestBody StoreRequest request) {
        StoreResponse updatedStore = storeService.updateStore(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sucursal actualizada correctamente", updatedStore));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sucursal eliminada correctamente", null));
    }

}
