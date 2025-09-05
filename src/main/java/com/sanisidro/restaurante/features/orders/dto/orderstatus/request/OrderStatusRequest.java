package com.sanisidro.restaurante.features.orders.dto.orderstatus.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusRequest {
    @NotBlank(message = "El nombre del estado es obligatorio")
    private String name;
}
