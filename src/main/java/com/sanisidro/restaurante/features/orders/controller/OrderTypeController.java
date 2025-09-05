package com.sanisidro.restaurante.features.orders.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.orders.dto.ordertype.request.OrderTypeRequest;
import com.sanisidro.restaurante.features.orders.dto.ordertype.response.OrderTypeResponse;
import com.sanisidro.restaurante.features.orders.service.OrderTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-types")
@RequiredArgsConstructor
public class OrderTypeController {

    private final OrderTypeService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderTypeResponse>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lista de tipos de orden", service.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderTypeResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tipo de orden encontrado", service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderTypeResponse>> create(@Valid @RequestBody OrderTypeRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tipo de orden creado", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderTypeResponse>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody OrderTypeRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tipo de orden actualizado", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tipo de orden eliminado", null));
    }

}
