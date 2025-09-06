package com.sanisidro.restaurante.core.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanisidro.restaurante.features.notifications.dto.NotificationEvent;
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
            NotificationEvent event = objectMapper.readValue(record.value(), NotificationEvent.class);
            notificationFacade.processNotification(event); // <--- aquí la magia
        } catch (Exception e) {
            logger.error("❌ Error al procesar el mensaje de Kafka", e);
        }
    }

}
