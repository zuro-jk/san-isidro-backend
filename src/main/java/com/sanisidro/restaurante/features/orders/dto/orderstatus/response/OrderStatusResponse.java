package com.sanisidro.restaurante.features.orders.dto.orderstatus.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String lang;
}
