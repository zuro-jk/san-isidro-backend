package com.sanisidro.restaurante.features.products.service;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.products.dto.product.request.ProductRequest;
import com.sanisidro.restaurante.features.products.dto.product.response.ProductResponse;
import com.sanisidro.restaurante.features.products.exceptions.CategoryNotFoundException;
import com.sanisidro.restaurante.features.products.exceptions.ProductNotFoundException;
import com.sanisidro.restaurante.features.products.model.Category;
import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.products.repository.CategoryRepository;
import com.sanisidro.restaurante.features.products.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductResponse> getAll() {
        List<ProductResponse> products = productRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
        return products;
    }

    public List<ProductResponse> getByCategory(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada con id: " + categoryId));

        List<ProductResponse> products = productRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToResponse)
                .toList();
        return products;
    }

    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con id: " + id));
        return mapToResponse(product);
    }

    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada con id: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .category(category)
                .imageUrl(request.getImageUrl())
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) >= 0) {
            product.setPrice(request.getPrice());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada con id: " + request.getCategoryId()));
            product.setCategory(category);
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    public Void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Producto no encontrado con id: " + id);
        }
        productRepository.deleteById(id);
        return null;
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
