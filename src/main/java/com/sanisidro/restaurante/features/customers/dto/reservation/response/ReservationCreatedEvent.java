package com.sanisidro.restaurante.features.customers.dto.reservation.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationCreatedEvent {
    private Long reservationId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private int numberOfPeople;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
}