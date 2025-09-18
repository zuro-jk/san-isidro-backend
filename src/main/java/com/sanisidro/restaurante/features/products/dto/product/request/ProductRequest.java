package com.sanisidro.restaurante.features.products.dto.product.request;

import com.sanisidro.restaurante.features.products.dto.productingredient.request.ProductIngredientRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private BigDecimal price;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    private String imageUrl;

    private List<@Valid ProductIngredientRequest> ingredients;
}
