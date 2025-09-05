package com.sanisidro.restaurante.features.orders.dto.ordertype.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderTypeRequest {
    @NotBlank(message = "El nombre del tipo de orden es obligatorio")
    private String name;
}
