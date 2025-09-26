package com.sanisidro.restaurante.features.orders.dto.paymentmethod.resposne;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String provider;
}