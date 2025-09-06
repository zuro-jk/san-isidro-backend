package com.sanisidro.restaurante.features.orders.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.orders.dto.orderstatus.request.OrderStatusRequest;
import com.sanisidro.restaurante.features.orders.dto.orderstatus.response.OrderStatusResponse;
import com.sanisidro.restaurante.features.orders.service.OrderStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-statuses")
@RequiredArgsConstructor
public class OrderStatusController {

    private final OrderStatusService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderStatusResponse>>> getAll(
            @RequestParam(defaultValue = "es") String lang) { // por defecto español
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lista de estados de orden", service.getAll(lang))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderStatusResponse>> getById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "es") String lang) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Estado de orden encontrado", service.getById(id, lang))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderStatusResponse>> create(
            @Valid @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Estado de orden creado", service.create(request))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderStatusResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Estado de orden actualizado", service.update(id, request))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Estado de orden eliminado", null));
    }

}
