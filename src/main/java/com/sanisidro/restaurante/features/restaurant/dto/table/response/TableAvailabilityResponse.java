package com.sanisidro.restaurante.features.restaurant.dto.table.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TableAvailabilityResponse {
    private Long id;
    private String name;
    private Integer capacity;
    private Integer minCapacity;
    private List<String> availableTimes;
}
