package com.sanisidro.restaurante.features.notifications.facade;

import com.sanisidro.restaurante.features.notifications.dto.NotifiableEvent;
import com.sanisidro.restaurante.features.notifications.dto.OrderNotificationEvent;
import com.sanisidro.restaurante.features.notifications.dto.ReservationNotificationEvent;
import com.sanisidro.restaurante.features.notifications.dto.StockLowNotificationEvent;
import com.sanisidro.restaurante.features.notifications.services.NotificationChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final Map<String, NotificationChannel> channels;

    public void processNotification(NotifiableEvent event) {
        String channelKey;

        if (event instanceof OrderNotificationEvent) {
            channelKey = "EMAIL";
        } else if (event instanceof ReservationNotificationEvent) {
            channelKey = "EMAIL";
        } else if (event instanceof StockLowNotificationEvent) {
            channelKey = "WEBSOCKET";
        } else {
            throw new IllegalArgumentException("Evento de notificación no soportado: " + event.getClass().getSimpleName());
        }

        NotificationChannel channel = channels.get(channelKey.toUpperCase());
        if (channel == null) {
            throw new IllegalArgumentException("Tipo de notificación no soportado: " + channelKey);
        }

        channel.send(event);
    }
}
