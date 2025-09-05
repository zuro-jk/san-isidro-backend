package com.sanisidro.restaurante.features.orders.controller;


import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.orders.dto.incident.request.IncidentRequest;
import com.sanisidro.restaurante.features.orders.dto.incident.response.IncidentResponse;
import com.sanisidro.restaurante.features.orders.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Incidentes obtenidos", incidentService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Incidente encontrado", incidentService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IncidentResponse>> create(@Valid @RequestBody IncidentRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Incidente creado", incidentService.create(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        incidentService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Incidente eliminado", null));
    }

}
