package com.sanisidro.restaurante.features.notifications.dto;

public interface NotifiableEvent {
    Long getUserId();

    String getRecipient();

    String getSubject();

    String getMessage();

    String getActionUrl();

    default String getChannelKey() {
        return "EMAIL";
    }

    default String getEventType() {
        return this.getClass().getSimpleName();
    }
}