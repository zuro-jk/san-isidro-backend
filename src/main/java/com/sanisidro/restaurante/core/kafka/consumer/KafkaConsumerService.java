package com.sanisidro.restaurante.core.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanisidro.restaurante.core.kafka.message.KafkaMessage;
import com.sanisidro.restaurante.features.notifications.dto.EmailVerificationEvent;
import com.sanisidro.restaurante.features.notifications.dto.NotifiableEvent;
import com.sanisidro.restaurante.features.notifications.dto.OrderNotificationEvent;
import com.sanisidro.restaurante.features.notifications.dto.ReservationNotificationEvent;
import com.sanisidro.restaurante.features.notifications.facade.NotificationFacade;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final ObjectMapper objectMapper;
    private final NotificationFacade notificationFacade;

    @KafkaListener(topics = "notifications", groupId = "san-isidro-group")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            KafkaMessage message = objectMapper.readValue(record.value(), KafkaMessage.class);

            String payloadJson = message.getPayload();

            NotifiableEvent event;
            if (payloadJson.contains("\"orderId\"")) {
                event = objectMapper.readValue(payloadJson, OrderNotificationEvent.class);
            } else if (payloadJson.contains("\"reservationId\"")) {
                event = objectMapper.readValue(payloadJson, ReservationNotificationEvent.class);
            } else if (payloadJson.contains("\"userId\"") && payloadJson.contains("\"actionUrl\"")) {
                event = objectMapper.readValue(payloadJson, EmailVerificationEvent.class);
            } else {
                logger.warn("⚠️ Tipo de evento desconocido: {}", payloadJson);
                return;
            }

            notificationFacade.processNotification(event);

            logger.info("✅ Evento procesado correctamente: key={}", message.getKey());

        } catch (Exception e) {
            logger.error("❌ Error al procesar el mensaje de Kafka", e);
        }
    }
}