package com.sanisidro.restaurante.features.products.dto.productingredient.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductIngredientResponse {
    private Long ingredientId;
    private String ingredientName;
    private String unitName;
    private String unitSymbol;
    private Double quantity;
}