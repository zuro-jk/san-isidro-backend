package com.sanisidro.restaurante.features.notifications.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanisidro.restaurante.features.notifications.dto.NotifiableEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service("WEBSOCKET")
@RequiredArgsConstructor
public class WebSocketNotificationChannel implements NotificationChannel {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void send(NotifiableEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            messagingTemplate.convertAndSend("/topic/notifications", payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar notificación WebSocket", e);
        }
    }

}
