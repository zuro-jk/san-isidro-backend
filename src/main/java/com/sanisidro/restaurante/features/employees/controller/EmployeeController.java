package com.sanisidro.restaurante.features.employees.controller;

import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.employees.dto.employee.request.EmployeeRequest;
import com.sanisidro.restaurante.features.employees.dto.employee.response.EmployeeResponse;
import com.sanisidro.restaurante.features.employees.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAll() {
        List<EmployeeResponse> employees = employeeService.getAll();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lista de empleados obtenida correctamente", employees)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getById(@PathVariable Long id) {
        EmployeeResponse employee = employeeService.getById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Empleado obtenido correctamente", employee)
        );
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeResponse>>> getAllPaged(Pageable pageable) {
        PagedResponse<EmployeeResponse> paged = employeeService.getAllPaged(pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Lista de empleados paginada obtenida correctamente", paged)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = employeeService.create(request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Empleado creado correctamente", created)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request
    ) {
        EmployeeResponse updated = employeeService.update(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Empleado actualizado correctamente", updated)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Empleado eliminado correctamente", null)
        );
    }
}