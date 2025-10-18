package com.sanisidro.restaurante.features.orders.dto.order.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryAddressRequest {

    private String street;
    private String reference;
    private String city;
    private String instructions;
    private String province;
    private String zipCode;

    @NotNull(message = "La latitud es obligatoria para el delivery")
    private Double latitude;

    @NotNull(message = "La longitud es obligatoria para el delivery")
    private Double longitude;

}
