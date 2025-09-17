package com.sanisidro.restaurante.features.products.service;

import com.sanisidro.restaurante.features.products.dto.product.request.ProductRequest;
import com.sanisidro.restaurante.features.products.dto.product.response.ProductDetailResponse;
import com.sanisidro.restaurante.features.products.dto.product.response.ProductResponse;
import com.sanisidro.restaurante.features.products.dto.productingredient.response.ProductIngredientResponse;
import com.sanisidro.restaurante.features.products.exceptions.CategoryNotFoundException;
import com.sanisidro.restaurante.features.products.exceptions.ProductNotFoundException;
import com.sanisidro.restaurante.features.products.model.Category;
import com.sanisidro.restaurante.features.products.model.Ingredient;
import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.products.model.ProductIngredient;
import com.sanisidro.restaurante.features.products.repository.CategoryRepository;
import com.sanisidro.restaurante.features.products.repository.IngredientRepository;
import com.sanisidro.restaurante.features.products.repository.ProductIngredientRepository;
import com.sanisidro.restaurante.features.products.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductIngredientRepository productIngredientRepository;

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProductResponse> getByCategory(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada con id: " + categoryId));

        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductDetailResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con id: " + id));
        return mapToDetailResponse(product);
    }

    @Transactional
    public ProductDetailResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada con id: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .category(category)
                .imageUrl(request.getImageUrl())
                .build();

        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            Set<ProductIngredient> ingredients = request.getIngredients().stream()
                    .map(ingReq -> {
                        Ingredient ingredient = ingredientRepository.findById(ingReq.getIngredientId())
                                .orElseThrow(() -> new EntityNotFoundException("Ingrediente no encontrado con id: " + ingReq.getIngredientId()));

                        return ProductIngredient.builder()
                                .product(product)
                                .ingredient(ingredient)
                                .quantity(ingReq.getQuantity())
                                .build();
                    })
                    .collect(Collectors.toSet());

            product.replaceIngredients(ingredients);
        }

        Product saved = productRepository.save(product);
        return mapToDetailResponse(saved);
    }

    @Transactional
    public ProductDetailResponse update(Long id, ProductRequest request) {
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

        if (request.getIngredients() != null) {
            Set<ProductIngredient> ingredients = request.getIngredients().stream()
                    .map(ingReq -> {
                        Ingredient ingredient = ingredientRepository.findById(ingReq.getIngredientId())
                                .orElseThrow(() -> new EntityNotFoundException("Ingrediente no encontrado con id: " + ingReq.getIngredientId()));

                        return ProductIngredient.builder()
                                .product(product)
                                .ingredient(ingredient)
                                .quantity(ingReq.getQuantity())
                                .build();
                    })
                    .collect(Collectors.toSet());

            product.replaceIngredients(ingredients);
        }

        Product updated = productRepository.save(product);
        return mapToDetailResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Producto no encontrado con id: " + id);
        }
        productRepository.deleteById(id);
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

    private ProductDetailResponse mapToDetailResponse(Product product) {
        List<ProductIngredientResponse> ingredientResponses = product.getIngredients() == null
                ? List.of()
                : product.getIngredients().stream()
                .map(pi -> ProductIngredientResponse.builder()
                        .ingredientId(pi.getIngredient().getId())
                        .ingredientName(pi.getIngredient().getName())
                        .unitName(pi.getIngredient().getUnit().getName())
                        .unitSymbol(pi.getIngredient().getUnit().getSymbol())
                        .quantity(pi.getQuantity())
                        .build()
                )
                .toList();

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .imageUrl(product.getImageUrl())
                .ingredients(ingredientResponses)
                .build();
    }
}
