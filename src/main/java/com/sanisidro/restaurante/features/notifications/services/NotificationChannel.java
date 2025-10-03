package com.sanisidro.restaurante.features.notifications.services;

import com.sanisidro.restaurante.features.notifications.dto.NotifiableEvent;

public interface NotificationChannel {
    void send(NotifiableEvent event);
}