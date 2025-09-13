package com.sanisidro.restaurante.features.restaurant.dto.table.response;

import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalTime;

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
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer reservationDurationMinutes;
    private Integer bufferBeforeMinutes;
    private Integer bufferAfterMinutes;
}
