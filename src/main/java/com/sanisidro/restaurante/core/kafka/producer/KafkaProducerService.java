package com.sanisidro.restaurante.core.kafka.producer;

import com.sanisidro.restaurante.core.kafka.message.KafkaMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(KafkaMessage message) {
        logger.info("📤 Enviando mensaje a Kafka: topic={}, key={}, payload={}",
                message.getTopic(), message.getKey(), message.getPayload());

        kafkaTemplate.send(message.getTopic(), message.getKey(), message.getPayload());
    }

}
