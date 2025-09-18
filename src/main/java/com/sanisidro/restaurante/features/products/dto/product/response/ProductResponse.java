package com.sanisidro.restaurante.features.products.dto.product.response;


import com.sanisidro.restaurante.features.products.dto.productingredient.response.ProductIngredientResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
}
