package com.sanisidro.restaurante.features.orders.dto.payment.request;

import java.math.BigDecimal;

import com.sanisidro.restaurante.features.orders.enums.PaymentStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentInOrderRequest {

    @NotNull(message = "El método de pago es obligatorio")
    private Long paymentMethodId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    @NotNull(message = "Debe indicarse si el pago es online o no")
    private Boolean isOnline;

    @Size(max = 100, message = "El código de transacción no puede superar 100 caracteres")
    private String transactionCode;

    private PaymentStatus status;
}