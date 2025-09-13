package com.sanisidro.restaurante.features.notifications.dto;

public interface NotifiableEvent {
    Long getUserId();
    String getRecipient();
    String getSubject();
    String getMessage();
    String getActionUrl();
}
