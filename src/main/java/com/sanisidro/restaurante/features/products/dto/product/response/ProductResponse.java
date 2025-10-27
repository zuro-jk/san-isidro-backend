package com.sanisidro.restaurante.features.products.dto.product.response;

import java.math.BigDecimal;
import java.util.List;

import com.sanisidro.restaurante.features.products.dto.productingredient.response.ProductIngredientResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private Integer preparationTimeMinutes;
    private List<ProductIngredientResponse> ingredients;
    private boolean active;
}
