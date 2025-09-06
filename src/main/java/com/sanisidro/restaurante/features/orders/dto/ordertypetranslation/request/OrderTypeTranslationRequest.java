package com.sanisidro.restaurante.features.orders.dto.ordertypetranslation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderTypeTranslationRequest {

    @NotNull(message = "El id del tipo no puede ser nulo")
    private Long orderTypeId;

    @NotBlank(message = "El idioma no puede ser vacío")
    private String lang;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;

    private String description;
}
