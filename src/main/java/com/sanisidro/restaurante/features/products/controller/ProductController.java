package com.sanisidro.restaurante.features.products.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.products.dto.product.request.ProductRequest;
import com.sanisidro.restaurante.features.products.dto.product.response.ProductResponse;
import com.sanisidro.restaurante.features.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAll() {
        List<ProductResponse> products = productService.getAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Productos obtenidos", products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        ProductResponse product = productService.getById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Producto obtenido", product));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCategory(@PathVariable Long categoryId) {
        List<ProductResponse> products = productService.getByCategory(categoryId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Productos por categoría obtenidos", products));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Producto creado", product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Producto actualizado", product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Producto eliminado", null));
    }
}