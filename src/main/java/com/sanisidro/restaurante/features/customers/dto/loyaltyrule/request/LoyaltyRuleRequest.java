package com.sanisidro.restaurante.features.customers.dto.loyaltyrule.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoyaltyRuleRequest {

    @NotBlank(message = "El nombre de la regla es obligatorio")
    private String name;

    @NotNull(message = "Los puntos son obligatorios")
    @Min(value = 0, message = "Los puntos deben ser mayores o iguales a 0")
    private Integer points;

    private Double minPurchaseAmount;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean active;

    @NotNull(message = "El campo perPerson es obligatorio")
    private Boolean perPerson;
}
