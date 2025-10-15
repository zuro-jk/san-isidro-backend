package com.sanisidro.restaurante.features.orders.dto.paymentmethod.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentMethodRequest {
    @NotBlank(message = "El código del método de pago es obligatorio")
    private String code;

    @NotBlank(message = "El nombre del método de pago es obligatorio")
    private String name;

    private String description;

    private String lang;
}
