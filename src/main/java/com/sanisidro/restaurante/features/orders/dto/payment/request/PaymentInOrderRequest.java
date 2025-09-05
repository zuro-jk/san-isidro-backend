package com.sanisidro.restaurante.features.orders.dto.payment.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentInOrderRequest {

    private Long id;

    @NotNull(message = "El método de pago es obligatorio")
    private Long paymentMethodId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal amount;

    private LocalDateTime date;

    @NotNull(message = "Debe indicar si el pago es en línea o presencial")
    private Boolean isOnline;

    @Size(max = 100, message = "El código de transacción no puede superar los 100 caracteres")
    private String transactionCode;
}
