package com.sanisidro.restaurante.features.suppliers.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.suppliers.dto.supplier.request.SupplierRequest;
import com.sanisidro.restaurante.features.suppliers.dto.supplier.response.SupplierResponse;
import com.sanisidro.restaurante.features.suppliers.service.SupplierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAll() {
        List<SupplierResponse> suppliers = supplierService.getAll();
        ApiResponse<List<SupplierResponse>> response = new ApiResponse<>(true,
                "Lista de Proveedores obtenida exitosamente",
                suppliers);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> getById(@PathVariable Long id) {
        SupplierResponse supplier = supplierService.getById(id);
        ApiResponse<SupplierResponse> response = new ApiResponse<>(true, "Proveedor obtenido exitosamente", supplier);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<SupplierResponse>> create(
            @Valid @RequestBody SupplierRequest request) {
        SupplierResponse supplier = supplierService.create(request);
        ApiResponse<SupplierResponse> response = new ApiResponse<>(true, "Proveedor creado exitosamente", supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> update(@PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {
        SupplierResponse supplier = supplierService.update(id, request);
        ApiResponse<SupplierResponse> response = new ApiResponse<>(true, "Proveedor actualizado exitosamente",
                supplier);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return new ApiResponse<>(true, "Proveedor eliminado exitosamente", null);
    }

}
