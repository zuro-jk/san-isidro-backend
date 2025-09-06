package com.sanisidro.restaurante.features.orders.dto.document.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DocumentRequest {

    @NotNull(message = "El ID de la orden es obligatorio")
    private Long orderId;

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String type;

    @NotBlank(message = "El número de documento es obligatorio")
    private String number;

    @NotNull(message = "El monto del documento es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
    private BigDecimal amount;
}