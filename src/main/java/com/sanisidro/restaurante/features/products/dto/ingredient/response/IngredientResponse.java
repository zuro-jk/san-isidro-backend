package com.sanisidro.restaurante.features.products.dto.ingredient.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientResponse {
    private Long id;
    private String name;
    private Long unitId;
    private String unitName;
    private String unitSymbol;
    private Integer stock;
    private Integer minStock;
}