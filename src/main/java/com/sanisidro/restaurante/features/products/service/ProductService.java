package com.sanisidro.restaurante.features.products.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.aws.service.FileService;
import com.sanisidro.restaurante.features.orders.repository.OrderDetailRepository;
import com.sanisidro.restaurante.features.products.dto.product.request.ProductRequest;
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
import com.sanisidro.restaurante.features.products.repository.ProductRepository;
import com.sanisidro.restaurante.features.reports.dto.response.ProductSalesReportResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

        private final ProductRepository productRepository;
        private final CategoryRepository categoryRepository;
        private final IngredientRepository ingredientRepository;
        private final OrderDetailRepository orderDetailRepository;
        private final FileService fileService;

        public List<ProductResponse> getAllActive() {
                return productRepository.findAllActive().stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        public List<ProductResponse> getAllIncludingInactive() {
                return productRepository.findAllIncludingInactive().stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        public ProductResponse getByIdSmart(Long id) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                boolean isAuthenticated = auth != null && auth.isAuthenticated()
                                && !"anonymousUser".equals(String.valueOf(auth.getPrincipal()));

                boolean isAdmin = isAuthenticated && auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN")
                                                || a.getAuthority().equalsIgnoreCase("ADMIN"));

                boolean isClient = isAuthenticated && auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_CLIENT")
                                                || a.getAuthority().equalsIgnoreCase("CLIENT"));

                Product product = productRepository.findByIdIncludingInactive(id)
                                .orElseThrow(() -> new ProductNotFoundException(
                                                "Producto no encontrado con id: " + id));

                if (product.isActive()) {
                        return mapToResponse(product);
                }

                if (isAdmin)
                        return mapToResponse(product);

                if (isClient) {
                        boolean orderedBefore = orderDetailRepository.existsByProduct_IdAndOrder_Customer_User_Username(
                                        id,
                                        auth.getName());
                        if (orderedBefore)
                                return mapToResponse(product);
                }

                throw new ProductNotFoundException("Producto no disponible actualmente");
        }

        public List<ProductResponse> getByCategory(Long categoryId) {
                categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new CategoryNotFoundException(
                                                "Categoría no encontrada con id: " + categoryId));

                return productRepository.findByCategoryId(categoryId).stream()
                                .filter(Product::isActive)
                                .map(this::mapToResponse)
                                .toList();
        }

        @Transactional
        public ProductResponse create(ProductRequest request) {
                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new CategoryNotFoundException(
                                                "Categoría no encontrada con id: " + request.getCategoryId()));

                Product product = Product.builder()
                                .name(request.getName())
                                .description(request.getDescription())
                                .price(request.getPrice())
                                .category(category)
                                .imageUrl(request.getImageUrl())
                                .preparationTimeMinutes(request.getPreparationTimeMinutes())
                                .active(request.getActive() != null ? request.getActive() : true)
                                .build();

                if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
                        Set<ProductIngredient> ingredients = request.getIngredients().stream()
                                        .map(ingReq -> {
                                                Ingredient ingredient = ingredientRepository
                                                                .findById(ingReq.getIngredientId())
                                                                .orElseThrow(() -> new EntityNotFoundException(
                                                                                "Ingrediente no encontrado con id: "
                                                                                                + ingReq.getIngredientId()));

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
                return mapToResponse(saved);
        }

        @Transactional
        public ProductResponse update(Long id, ProductRequest request) {
                Product product = productRepository.findByIdIncludingInactive(id)
                                .orElseThrow(() -> new ProductNotFoundException(
                                                "Producto no encontrado con id: " + id));

                String previousImageUrl = product.getImageUrl();

                if (request.getName() != null && !request.getName().isBlank()) {
                        product.setName(request.getName());
                }

                if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) >= 0) {
                        product.setPrice(request.getPrice());
                }

                if (request.getCategoryId() != null) {
                        Category category = categoryRepository.findById(request.getCategoryId())
                                        .orElseThrow(() -> new CategoryNotFoundException(
                                                        "Categoría no encontrada con id: " + request.getCategoryId()));
                        product.setCategory(category);
                }

                if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
                        if (previousImageUrl != null && !previousImageUrl.isBlank()) {
                                fileService.deleteFileByUrl(previousImageUrl);
                        }
                        product.setImageUrl(null);
                } else if (!request.getImageUrl().equals(previousImageUrl)) {
                        if (previousImageUrl != null && !previousImageUrl.isBlank()) {
                                fileService.deleteFileByUrl(previousImageUrl);
                        }
                        product.setImageUrl(request.getImageUrl());
                }

                if (request.getActive() != null) {
                        product.setActive(request.getActive());
                }

                if (request.getPreparationTimeMinutes() != null && request.getPreparationTimeMinutes() > 0) {
                        product.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
                }

                if (request.getIngredients() != null) {
                        Set<ProductIngredient> ingredients = request.getIngredients().stream()
                                        .map(ingReq -> {
                                                Ingredient ingredient = ingredientRepository
                                                                .findById(ingReq.getIngredientId())
                                                                .orElseThrow(() -> new EntityNotFoundException(
                                                                                "Ingrediente no encontrado con id: "
                                                                                                + ingReq.getIngredientId()));

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
                return mapToResponse(updated);
        }

        @Transactional
        public ProductResponse toggleActive(Long id) {
                Product product = productRepository.findByIdIncludingInactive(id)
                                .orElseThrow(() -> new ProductNotFoundException(
                                                "Producto no encontrado con id: " + id));

                product.setActive(!product.isActive());
                Product updated = productRepository.save(product);

                return mapToResponse(updated);
        }

        @Transactional
        public void delete(Long id) {
                Product product = productRepository.findByIdIncludingInactive(id)
                                .orElseThrow(() -> new ProductNotFoundException(
                                                "Producto no encontrado con id: " + id));

                productRepository.delete(product);
        }

        public List<ProductSalesReportResponse> getTopSellingProducts() {
                return orderDetailRepository.findTopSellingProducts().stream()
                                .map((Object[] row) -> {
                                        Number idNum = (Number) row[0];
                                        Long productId = idNum != null ? idNum.longValue() : null;

                                        String productName = row[1] != null ? row[1].toString() : null;

                                        Number qtyNum = (Number) row[2];
                                        Long totalQuantitySold = qtyNum != null ? qtyNum.longValue() : 0L;

                                        BigDecimal totalRevenue;
                                        if (row[3] == null) {
                                                totalRevenue = BigDecimal.ZERO;
                                        } else if (row[3] instanceof BigDecimal) {
                                                totalRevenue = (BigDecimal) row[3];
                                        } else {
                                                totalRevenue = new BigDecimal(row[3].toString());
                                        }

                                        return ProductSalesReportResponse.builder()
                                                        .productId(productId)
                                                        .productName(productName)
                                                        .totalQuantitySold(totalQuantitySold)
                                                        .totalRevenue(totalRevenue)
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        private ProductResponse mapToResponse(Product product) {
                List<ProductIngredientResponse> ingredientResponses = product.getIngredients() == null
                                ? List.of()
                                : product.getIngredients().stream()
                                                .map(pi -> ProductIngredientResponse.builder()
                                                                .ingredientId(pi.getIngredient().getId())
                                                                .ingredientName(pi.getIngredient().getName())
                                                                .unitName(pi.getIngredient().getUnit().getName())
                                                                .unitSymbol(pi.getIngredient().getUnit().getSymbol())
                                                                .quantity(pi.getQuantity())
                                                                .build())
                                                .toList();

                return ProductResponse.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .description(product.getDescription())
                                .price(product.getPrice())
                                .categoryId(product.getCategory().getId())
                                .categoryName(product.getCategory().getName())
                                .imageUrl(product.getImageUrl())
                                .preparationTimeMinutes(product.getPreparationTimeMinutes())
                                .ingredients(ingredientResponses)
                                .active(product.isActive())
                                .build();
        }

}
