package com.sanisidro.restaurante.features.notifications.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ReservationNotificationEvent implements NotifiableEvent {
    private Long userId;
    private String recipient;
    private String subject;
    private String message;
    private String actionUrl;

    private Long reservationId;
    private LocalDateTime reservationDate;
    private String reservationTime;
    private int numberOfPeople;
    private String customerName;

    private String tableName;
}