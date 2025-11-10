package com.sanisidro.restaurante.features.products.dto.product.request;

import java.math.BigDecimal;
import java.util.List;

import com.sanisidro.restaurante.features.products.dto.comboproductitem.request.ComboItemRequest;
import com.sanisidro.restaurante.features.products.dto.productingredient.request.ProductIngredientRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProductRequest {
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    @NotBlank(message = "La descripción del producto es obligatoria")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal price;

    private String imageUrl;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    @NotNull(message = "El tiempo de preparación es obligatorio")
    @Min(value = 1, message = "El tiempo de preparación debe ser mayor a 0 minutos")
    private Integer preparationTimeMinutes;

    private Boolean active;

    private boolean isCombo;

    @Valid
    private List<ProductIngredientRequest> ingredients;

    private List<ComboItemRequest> comboItems;
}
