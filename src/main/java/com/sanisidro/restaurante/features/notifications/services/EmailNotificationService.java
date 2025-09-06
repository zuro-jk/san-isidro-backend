package com.sanisidro.restaurante.features.notifications.services;

import com.sanisidro.restaurante.core.email.dto.request.EmailMessageRequest;
import com.sanisidro.restaurante.core.email.service.EmailService;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.notifications.dto.NotificationEvent;
import com.sanisidro.restaurante.features.notifications.enums.NotificationStatus;
import com.sanisidro.restaurante.features.notifications.model.EmailNotification;
import com.sanisidro.restaurante.features.notifications.repository.EmailNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("EMAIL")
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationChannel {

    private final EmailService emailService;
    private final EmailNotificationRepository emailNotificationRepository;
    private final UserRepository userRepository;

    public void send(NotificationEvent event) {
        String recipient = event.getRecipient();
        Long userId = event.getUserId();
        User user = null;

        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
            if (recipient == null || recipient.isBlank()) {
                recipient = user != null ? user.getEmail() : null;
            }
        }

        if (recipient == null || recipient.isBlank()) {
            log.warn("⚠️ Notificación fallida: no hay destinatario válido para userId={}", userId);

            EmailNotification failedEmail = EmailNotification.builder()
                    .toAddress(null)
                    .subject(event.getSubject())
                    .body(event.getMessage())
                    .sentAt(LocalDateTime.now())
                    .status(NotificationStatus.FAILED)
                    .user(user)
                    .build();
            emailNotificationRepository.save(failedEmail);
            return;
        }


        EmailNotification email = EmailNotification.builder()
                .toAddress(recipient)
                .subject(event.getSubject())
                .body(event.getMessage())
                .sentAt(LocalDateTime.now())
                .status(NotificationStatus.PENDING)
                .user(user)
                .build();

        emailNotificationRepository.save(email);

        try {
            EmailMessageRequest request = EmailMessageRequest.builder()
                    .toAddress(recipient)
                    .subject(event.getSubject())
                    .body(event.getMessage())
                    .build();

            emailService.sendEmail(request);
            email.setStatus(NotificationStatus.SENT);

            log.info("📧 Notificación enviada a {}", recipient);
        } catch (Exception e) {
            email.setStatus(NotificationStatus.FAILED);
            log.error("❌ Fallo al enviar notificación a {}: {}", recipient, e.getMessage(), e);
        }

        emailNotificationRepository.save(email);
    }

}
