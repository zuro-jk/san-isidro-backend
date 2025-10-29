package com.sanisidro.restaurante.features.products.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.sanisidro.restaurante.features.products.dto.product.request.ProductRequest;
import com.sanisidro.restaurante.features.products.dto.product.response.ProductResponse;
import com.sanisidro.restaurante.features.products.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Público: todos ven productos activos
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllActive() {
        List<ProductResponse> products = productService.getAllActive();
        return ResponseEntity.ok(new ApiResponse<>(true, "Productos activos obtenidos", products));
    }

    // Solo admin: lista todo, incluyendo inactivos
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllIncludingInactive() {
        List<ProductResponse> products = productService.getAllIncludingInactive();
        return ResponseEntity.ok(new ApiResponse<>(true, "Todos los productos obtenidos", products));
    }

    // Detalle inteligente según rol
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        ProductResponse product = productService.getByIdSmart(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Producto obtenido", product));
    }

    // Público: ver productos activos por categoría
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCategory(@PathVariable Long categoryId) {
        List<ProductResponse> products = productService.getByCategory(categoryId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Productos por categoría obtenidos", products));
    }

    // Solo admin
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Producto creado", product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Producto actualizado", product));
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleActive(@PathVariable Long id) {
        ProductResponse updated = productService.toggleActive(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Estado del producto actualizado", updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Producto eliminado", null));
    }
}