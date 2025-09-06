package com.sanisidro.restaurante.features.orders.dto.orderstatustranslation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusTranslationRequest {

    @NotNull(message = "El id del estado no puede ser nulo")
    private Long orderStatusId;

    @NotBlank(message = "El idioma no puede ser vacío")
    private String lang;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;

    private String description;
}
