package com.sanisidro.restaurante.features.orders.dto.ordertypetranslation.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderTypeTranslationResponse {
    private Long id;
    private Long orderTypeId;
    private String lang;
    private String name;
    private String description;
}
