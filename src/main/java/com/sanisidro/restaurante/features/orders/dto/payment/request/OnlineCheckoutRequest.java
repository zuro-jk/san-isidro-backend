package com.sanisidro.restaurante.features.orders.dto.payment.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OnlineCheckoutRequest {

    @NotNull(message = "El ID de la orden es obligatorio")
    private Long orderId;

    @NotBlank(message = "El proveedor es obligatorio (ej: MERCADOPAGO, PAYPAL)")
    private String provider;

    @NotBlank(message = "El token es obligatorio")
    private String token;

}