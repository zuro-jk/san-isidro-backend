package com.sanisidro.restaurante.features.restaurant.dto.store.response;

import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreResponse {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private LocalTime openTime;
    private LocalTime closeTime;

}
