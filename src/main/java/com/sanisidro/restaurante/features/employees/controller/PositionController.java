package com.sanisidro.restaurante.features.employees.controller;

import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.employees.dto.employee.request.PositionRequest;
import com.sanisidro.restaurante.features.employees.dto.employee.response.PositionResponse;
import com.sanisidro.restaurante.features.employees.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getAll() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lista de puestos obtenida correctamente", positionService.getAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Puesto obtenido correctamente", positionService.getById(id))
        );
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<PagedResponse<PositionResponse>>> getAllPaged(Pageable pageable) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lista paginada de puestos obtenida correctamente", positionService.getAllPaged(pageable))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PositionResponse>> create(@Valid @RequestBody PositionRequest request) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Puesto creado correctamente", positionService.create(request))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PositionRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Puesto actualizado correctamente", positionService.update(id, request))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        positionService.delete(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Puesto eliminado correctamente", null)
        );
    }

}
