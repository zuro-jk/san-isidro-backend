package com.sanisidro.restaurante.features.orders.dto.paymentmethod.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentMethodRequest {
    @NotBlank(message = "El código del método de pago es obligatorio")
    private String code; // identificador interno del método

    @NotBlank(message = "El nombre del método de pago es obligatorio")
    private String name; // nombre en el idioma especificado

    private String description; // descripción opcional

    private String lang; // idioma de la traducción, ej. "es", "en", "pt"
}
