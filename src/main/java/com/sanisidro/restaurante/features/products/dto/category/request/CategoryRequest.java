package com.sanisidro.restaurante.features.products.dto.category.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String name;
}
