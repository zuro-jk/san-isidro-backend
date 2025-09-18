package com.sanisidro.restaurante.features.products.dto.unit.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitRequest {

    @NotBlank(message = "El nombre de la unidad es obligatorio")
    private String name;

    @NotBlank(message = "El símbolo de la unidad es obligatorio")
    private String symbol;
}