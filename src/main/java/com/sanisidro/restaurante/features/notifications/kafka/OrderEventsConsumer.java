package com.sanisidro.restaurante.features.notifications.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanisidro.restaurante.features.notifications.dto.NotificationEvent;
import com.sanisidro.restaurante.features.notifications.facade.NotificationFacade;
import com.sanisidro.restaurante.features.orders.dto.order.response.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventsConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventsConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificationFacade notificationFacade;

    @KafkaListener(topics = "orders", groupId = "san-isidro-group")
    public void listen(ConsumerRecord<String, String> record) {
        logger.info("📥 Evento de orden recibido: {}", record.value());
        try {
            OrderCreatedEvent event = objectMapper.readValue(record.value(), OrderCreatedEvent.class);

            NotificationEvent notification = NotificationEvent.builder()
                    .userId(event.getCustomerId())
                    .type("EMAIL")
                    .recipient(event.getCustomerEmail()) // si viene, se usa; si no, facade puede buscar por userId
                    .subject("Confirmación de tu orden #" + event.getOrderId())
                    .message("Hola " + event.getCustomerName() +
                            ", tu orden #" + event.getOrderId() +
                            " fue registrada. Total: " + event.getTotal() + " .")
                    .build();

            notificationFacade.processNotification(notification);

        } catch (Exception e) {
            logger.error("❌ Error procesando evento de orden", e);
        }
    }

}
