package com.sanisidro.restaurante.features.notifications.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.notifications.dto.NotificationEvent;
import com.sanisidro.restaurante.features.notifications.facade.NotificationFacade;
import com.sanisidro.restaurante.features.orders.dto.order.response.OrderCreatedEvent;
import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.model.OrderDetail;
import com.sanisidro.restaurante.features.orders.service.OrderEmailBuilder;
import com.sanisidro.restaurante.features.products.model.Product;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;
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
                    .subject("Confirmación de tu orden #" + event.getOrderId())
                    .message(OrderEmailBuilder.buildOrderCreatedEmail(mapToOrder(event)))
                    .build();

            notificationFacade.processNotification(notification);

        } catch (Exception e) {
            logger.error("❌ Error procesando evento de orden", e);
        }
    }

    private Order mapToOrder(OrderCreatedEvent event) {
        Order order = new Order();
        order.setId(event.getOrderId());

        Customer customer = new Customer();
        customer.setId(event.getCustomerId());
        User user = User.builder()
                .firstName(event.getCustomerName().split(" ")[0])
                .lastName(event.getCustomerName().split(" ").length > 1 ? event.getCustomerName().split(" ")[1] : "")
                .email(event.getCustomerEmail())
                .build();
        customer.setUser(user);
        order.setCustomer(customer);

        Set<OrderDetail> details = event.getProducts().stream().map(p -> {
            OrderDetail detail = new OrderDetail();
            Product product = new Product();
            product.setName(p.getName());
            detail.setProduct(product);
            detail.setQuantity(p.getQuantity());
            detail.setUnitPrice(p.getUnitPrice());
            return detail;
        }).collect(Collectors.toCollection(LinkedHashSet::new));

        order.setDetails(details);
        order.setTotal(event.getTotal());

        return order;
    }

}
