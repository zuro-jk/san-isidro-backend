package com.sanisidro.restaurante.features.notifications.controller;

import com.sanisidro.restaurante.core.email.service.EmailService;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.features.notifications.dto.ContactNotificationEvent;
import com.sanisidro.restaurante.features.notifications.kafka.NotificationProducer;
import com.sanisidro.restaurante.features.notifications.services.EmailNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationProducer notificationProducer;
    private final EmailNotificationService emailNotificationService;

    @PostMapping
    public ResponseEntity<String> sendContact(
            @Valid @RequestBody ContactNotificationEvent contact,
            @AuthenticationPrincipal User user
    ) {
        ContactNotificationEvent event = ContactNotificationEvent.builder()
                .userId(user.getId())
                .subject(contact.getSubject())
                .message("Mensaje de " + user.getFullName() + " (" + user.getEmail() + "):\n\n" + contact.getMessage())
                .actionUrl(contact.getActionUrl())
                .build();

//        notificationProducer.send("notifications", event);
        emailNotificationService.send(event);

        return ResponseEntity.ok("Mensaje enviado correctamente. Gracias por contactarnos!");
    }
}