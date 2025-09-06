package com.sanisidro.restaurante.features.orders.dto.orderstatustranslation.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusTranslationResponse {
    private Long id;
    private Long orderStatusId;
    private String lang;
    private String name;
    private String description;
}
