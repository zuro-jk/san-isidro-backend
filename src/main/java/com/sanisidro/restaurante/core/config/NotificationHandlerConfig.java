package com.sanisidro.restaurante.core.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sanisidro.restaurante.features.notifications.dto.ContactNotificationEvent;
import com.sanisidro.restaurante.features.notifications.dto.EmailVerificationEvent;
import com.sanisidro.restaurante.features.notifications.dto.NotifiableEvent;
import com.sanisidro.restaurante.features.notifications.dto.OrderNotificationEvent;
import com.sanisidro.restaurante.features.notifications.dto.ReservationNotificationEvent;
import com.sanisidro.restaurante.features.notifications.services.EmailNotificationService;
import com.sanisidro.restaurante.features.notifications.services.NotificationHandler;

@Configuration
public class NotificationHandlerConfig {

    @Bean
    public Map<String, Map<String, NotificationHandler<? extends NotifiableEvent>>> notificationHandlers(
            EmailNotificationService emailService) {
        Map<String, Map<String, NotificationHandler<? extends NotifiableEvent>>> handlers = new HashMap<>();

        // Canal EMAIL
        Map<String, NotificationHandler<? extends NotifiableEvent>> emailHandlers = new HashMap<>();
        emailHandlers.put(EmailVerificationEvent.class.getSimpleName(), emailService);
        emailHandlers.put(OrderNotificationEvent.class.getSimpleName(), emailService);
        emailHandlers.put(ReservationNotificationEvent.class.getSimpleName(), emailService);
        emailHandlers.put(ContactNotificationEvent.class.getSimpleName(), emailService);

        handlers.put("EMAIL", emailHandlers);

        Map<String, NotificationHandler<? extends NotifiableEvent>> wsHandlers = new HashMap<>();
        handlers.put("WEBSOCKET", wsHandlers);

        return handlers;
    }

}
