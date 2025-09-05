package com.sanisidro.restaurante.features.orders.dto.paymentmethodtranslation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentMethodTranslationRequest {
    @NotNull(message = "El id del metodo de pago no puede ser nulo")
    private Long paymentMethodId;

    @NotBlank(message = "El idioma no puede ser vacio")
    private String lang;

    @NotBlank(message = "El nombre no puede estar vacio")
    private String name;

    private String description;
}
