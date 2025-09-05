package com.sanisidro.restaurante.features.orders.dto.incident.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IncidentResponse {
    private Long id;
    private Long userId;
    private Long orderId;
    private Long productId;
    private Long supplierId;
    private String type;
    private String description;
    private LocalDateTime date;
    private String status;
}