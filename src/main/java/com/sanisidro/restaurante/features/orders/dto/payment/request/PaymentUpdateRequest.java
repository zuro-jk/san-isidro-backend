package com.sanisidro.restaurante.features.orders.dto.payment.request;

import com.sanisidro.restaurante.features.orders.enums.PaymentStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentUpdateRequest {

    @Size(max = 100, message = "El código de transacción no puede superar los 100 caracteres")
    private String transactionCode;

    private PaymentStatus status;
}