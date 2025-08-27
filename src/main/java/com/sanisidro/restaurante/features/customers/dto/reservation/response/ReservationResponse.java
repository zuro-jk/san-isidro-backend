package com.sanisidro.restaurante.features.customers.dto.reservation.response;

import com.sanisidro.restaurante.features.customers.enums.ReservationStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id;

    private Long customerId;

    private String contactName;

    private String contactPhone;

    private LocalDate reservationDate;

    private LocalTime reservationTime;

    private Integer numberOfPeople;

    private ReservationStatus status;
}
