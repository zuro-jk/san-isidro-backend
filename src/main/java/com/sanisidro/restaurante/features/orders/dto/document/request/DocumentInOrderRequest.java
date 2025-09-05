package com.sanisidro.restaurante.features.orders.dto.document.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DocumentInOrderRequest {

    private Long id; // opcional para updates

    @NotNull(message = "El tipo de documento es obligatorio")
    @Size(max = 20, message = "El tipo de documento no puede superar los 20 caracteres")
    private String type;

    @NotNull(message = "El número de documento es obligatorio")
    @Size(max = 50, message = "El número de documento no puede superar los 50 caracteres")
    private String number;

    private LocalDateTime date; // si no se envía, se asigna LocalDateTime.now()

    @NotNull(message = "El monto del documento es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal amount;
}