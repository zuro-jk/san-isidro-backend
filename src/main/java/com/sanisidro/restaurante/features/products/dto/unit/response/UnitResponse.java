package com.sanisidro.restaurante.features.products.dto.unit.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitResponse {
    private Long id;
    private String name;
    private String symbol;
}