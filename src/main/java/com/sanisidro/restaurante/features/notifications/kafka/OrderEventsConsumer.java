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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderEventsConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventsConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificationFacade notificationFacade;

    @KafkaListener(topics = "orders", groupId = "foraneos-group")
    public void listen(ConsumerRecord<String, String> record) {
        logger.info("📥 Evento de orden recibido: {}", record.value());
        try {
            OrderCreatedEvent event = objectMapper.readValue(record.value(), OrderCreatedEvent.class);

            NotificationEvent notification = NotificationEvent.builder()
                    .userId(event.getCustomerId())
                    .type("EMAIL")
                    .recipient(event.getCustomerEmail())
                    .subject("¡Tu orden #" + event.getOrderId() + " ha sido confirmada!")
                    .products(mapProducts(event))
                    .total(event.getTotal())
                    .orderId(event.getOrderId())
                    .orderDate(event.getCreatedAt())
                    .message("¡Wow! Gracias por tu compra. Tu orden está en proceso.")
                    .actionUrl("https://miapp.com/orders/" + event.getOrderId())
                    .build();

            notificationFacade.processNotification(notification);

        } catch (Exception e) {
            logger.error("❌ Error procesando evento de orden", e);
        }
    }

    private List<NotificationEvent.OrderProduct> mapProducts(OrderCreatedEvent event) {
        return event.getProducts().stream()
                .map(p -> NotificationEvent.OrderProduct.builder()
                        .name(p.getName())
                        .quantity(p.getQuantity())
                        .unitPrice(p.getUnitPrice() != null ? p.getUnitPrice() : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }
}
