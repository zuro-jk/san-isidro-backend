package com.sanisidro.restaurante.features.orders.dto.orderstatus.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusRequest {
    @NotBlank(message = "El código del estado es obligatorio")
    private String code;

    @NotBlank(message = "El nombre del estado es obligatorio")
    private String name;

    private String description;

    @NotBlank(message = "El idioma es obligatorio")
    private String lang;
}
