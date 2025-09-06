package com.sanisidro.restaurante.features.notifications.facade;

import com.sanisidro.restaurante.features.notifications.dto.NotificationEvent;
import com.sanisidro.restaurante.features.notifications.services.EmailNotificationService;
import com.sanisidro.restaurante.features.notifications.services.NotificationChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

private final Map<String, NotificationChannel> channels;

    public void processNotification(NotificationEvent event) {
        NotificationChannel channel = channels.get(event.getType().toUpperCase());
        if (channel == null) {
            throw new IllegalArgumentException("Tipo de notificación no soportado: " + event.getType());
        }
        channel.send(event);
    }
}
