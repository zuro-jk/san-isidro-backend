package com.sanisidro.restaurante.core.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanisidro.restaurante.features.notifications.dto.NotifiableEvent;
import com.sanisidro.restaurante.features.notifications.dto.OrderNotificationEvent;
import com.sanisidro.restaurante.features.notifications.dto.ReservationNotificationEvent;
import com.sanisidro.restaurante.features.notifications.facade.NotificationFacade;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final ObjectMapper objectMapper;
    private final NotificationFacade notificationFacade;

    @KafkaListener(topics = "notifications", groupId = "san-isidro-group")
    public void listen(ConsumerRecord<String, String> record) {
        logger.info("📥 Mensaje recibido de Kafka: topic={}, key={}, value={}",
                record.topic(), record.key(), record.value());

        try {
            String json = record.value();

            NotifiableEvent event;
            if (json.contains("\"orderId\"")) {
                event = objectMapper.readValue(json, OrderNotificationEvent.class);
            } else if (json.contains("\"reservationId\"")) {
                event = objectMapper.readValue(json, ReservationNotificationEvent.class);
            } else {
                logger.warn("⚠️ Tipo de evento desconocido: {}", json);
                return;
            }

            notificationFacade.processNotification(event);

        } catch (Exception e) {
            logger.error("❌ Error al procesar el mensaje de Kafka", e);
        }
    }
}
