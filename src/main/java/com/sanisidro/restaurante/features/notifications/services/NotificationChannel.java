package com.sanisidro.restaurante.features.notifications.services;

import com.sanisidro.restaurante.features.notifications.dto.NotifiableEvent;
import com.sanisidro.restaurante.features.notifications.dto.OrderNotificationEvent;

public interface NotificationChannel {
    void send(NotifiableEvent event);
}