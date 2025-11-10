package com.sanisidro.restaurante.features.restaurant.dto.table.response;

import com.sanisidro.restaurante.features.restaurant.enums.TableStatus;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableResponse {
    private Long id;
    private String code;
    private String alias;
    private Integer capacity;
    private Integer minCapacity;
    private Integer optimalCapacity;
    private Integer priority;
    private String description;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer reservationDurationMinutes;
    private Integer bufferBeforeMinutes;
    private Integer bufferAfterMinutes;
    private TableStatus status;

    private Long activeOrderId;
}
