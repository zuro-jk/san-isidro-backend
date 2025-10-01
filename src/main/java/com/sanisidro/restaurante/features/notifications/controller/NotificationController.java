package com.sanisidro.restaurante.features.notifications.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.features.notifications.dto.ContactNotificationEvent;
import com.sanisidro.restaurante.features.notifications.kafka.NotificationProducer;
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

    @PostMapping
    public ResponseEntity<ApiResponse<String>> sendContact(
            @Valid @RequestBody ContactNotificationEvent contact,
            @AuthenticationPrincipal User user
    ) {
        String senderName;
        String senderEmail;

        if (user != null) {
            senderName = user.getFullName();
            senderEmail = user.getEmail();
        } else {
            if (contact.getName() == null || contact.getEmail() == null) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(false, "Nombre y email son obligatorios para usuarios anónimos", null)
                );
            }
            senderName = contact.getName();
            senderEmail = contact.getEmail();
        }

        ContactNotificationEvent event = ContactNotificationEvent.builder()
                .userId(user != null ? user.getId() : null)
                .subject(contact.getSubject())
                .message("Mensaje de " + senderName + " (" + senderEmail + "):\n\n" + contact.getMessage())
                .actionUrl(contact.getActionUrl())
                .build();

        notificationProducer.send("notifications", event);

        return ResponseEntity.ok(new ApiResponse<>(true, "Mensaje enviado correctamente. Gracias por contactarnos!", null));
    }
}