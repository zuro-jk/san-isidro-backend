package com.sanisidro.restaurante.features.notifications.kafka;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanisidro.restaurante.core.kafka.message.KafkaMessage;
import com.sanisidro.restaurante.features.notifications.dto.NotifiableEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(String topic, NotifiableEvent event) {
        try {
            String payloadJson = objectMapper.writeValueAsString(event);

            KafkaMessage message = KafkaMessage.builder()
                    .topic(topic)
                    .key(event.getClass().getSimpleName() + "-" + System.currentTimeMillis())
                    .payload(objectMapper.writeValueAsString(event))
                    .timestamp(LocalDateTime.now())
                    .build();

            String envelope = objectMapper.writeValueAsString(message);

            kafkaTemplate.send(topic, message.getKey(), envelope);

            log.info("📤 Evento publicado en Kafka: topic={}, key={}, payload={}",
                    topic, message.getKey(), payloadJson);

        } catch (JsonProcessingException e) {
            log.error("❌ Error serializando evento: {}", event, e);
            throw new RuntimeException("Error al serializar evento Kafka", e);
        }
    }
}