package com.sanisidro.restaurante.features.products.controller;

import com.sanisidro.restaurante.features.products.dto.unit.request.UnitRequest;
import com.sanisidro.restaurante.features.products.dto.unit.response.UnitResponse;
import com.sanisidro.restaurante.features.products.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
public class UnitController {
    private final UnitService unitService;

    @PostMapping
    public ResponseEntity<UnitResponse> create(@Valid @RequestBody UnitRequest request) {
        return ResponseEntity.ok(unitService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<UnitResponse>> findAll() {
        return ResponseEntity.ok(unitService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnitResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.getById(id));
    }
}
