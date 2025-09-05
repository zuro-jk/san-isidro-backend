package com.sanisidro.restaurante.features.orders.dto.incident.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IncidentRequest {
    @NotNull(message = "El id del usuario no puede ser nulo")
    private Long userId;

    private Long orderId;
    private Long productId;
    private Long supplierId;

    @NotBlank(message = "El tipo de incidente no puede estar vacio")
    private String type;

    @NotBlank(message = "La descripcion del incidente no puede estar vacio")
    private String description;

    private String status; // opcional, default OPEN
}