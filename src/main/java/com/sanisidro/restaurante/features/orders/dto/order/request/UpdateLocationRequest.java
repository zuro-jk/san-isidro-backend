package com.sanisidro.restaurante.features.orders.dto.order.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateLocationRequest {
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;
}