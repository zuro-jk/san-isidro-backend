package com.sanisidro.restaurante.features.restaurant.dto.table.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableResponse {
    private Long id;
    private String name;
    private Integer capacity;
    private String description;
}
