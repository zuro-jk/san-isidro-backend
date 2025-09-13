package com.sanisidro.restaurante.features.notifications.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanisidro.restaurante.features.notifications.dto.ReservationNotificationEvent;
import com.sanisidro.restaurante.features.notifications.facade.NotificationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationEventsConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationFacade notificationFacade;

    @KafkaListener(topics = "reservations", groupId = "reservations-group")
    public void listen(ConsumerRecord<String, String> record) {
        log.info("📥 Evento de reserva recibido: {}", record.value());

        try {
            ReservationNotificationEvent event =
                    objectMapper.readValue(record.value(), ReservationNotificationEvent.class);
            notificationFacade.processNotification(event);
        } catch (Exception e) {
            log.error("❌ Error procesando evento de reserva", e);
        }
    }
}