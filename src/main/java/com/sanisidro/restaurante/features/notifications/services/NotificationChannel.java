package com.sanisidro.restaurante.features.notifications.services;

import com.sanisidro.restaurante.features.notifications.dto.NotificationEvent;

public interface NotificationChannel {
    void send(NotificationEvent event);
}