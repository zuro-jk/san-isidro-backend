package com.sanisidro.restaurante.features.notifications.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {
    private Long userId;
    private String type;
    private String recipient;
    private String subject;
    private String message;
}
