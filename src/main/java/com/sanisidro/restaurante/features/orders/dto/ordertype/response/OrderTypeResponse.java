package com.sanisidro.restaurante.features.orders.dto.ordertype.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderTypeResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String lang;
}