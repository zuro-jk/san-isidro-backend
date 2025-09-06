package com.sanisidro.restaurante.features.orders.dto.paymentmethod.resposne;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodResponse {
    private Long id;
    private String code; // identificador interno
    private String name; // nombre traducido según el idioma solicitado
    private String description; // descripción traducida
}