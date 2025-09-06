package com.sanisidro.restaurante.features.orders.dto.paymentmethodtranslation.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodTranslationResponse {
    private Long id;
    private Long paymentMethodId;
    private String lang;
    private String name;
    private String description;
}
