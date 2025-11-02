package com.sanisidro.restaurante.features.orders.dto.order.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateStatusRequest {
    @NotBlank(message = "El nuevo código de estado es obligatorio")
    private String newStatusCode;
}