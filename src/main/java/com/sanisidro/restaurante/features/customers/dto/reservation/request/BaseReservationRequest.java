package com.sanisidro.restaurante.features.customers.dto.reservation.request;

import java.time.LocalDate;
import java.time.LocalTime;

public interface BaseReservationRequest {
    Long getTableId();
    String getContactName();
    String getContactPhone();
    LocalDate getReservationDate();
    LocalTime getReservationTime();
    Integer getNumberOfPeople();
}
