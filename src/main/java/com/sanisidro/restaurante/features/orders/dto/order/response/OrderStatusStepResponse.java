package com.sanisidro.restaurante.features.orders.dto.order.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusStepResponse {
    private String code;
    private String name;
    private int step;
}