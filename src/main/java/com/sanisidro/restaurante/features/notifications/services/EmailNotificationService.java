package com.sanisidro.restaurante.features.notifications.services;

import com.sanisidro.restaurante.core.email.dto.request.EmailMessageRequest;
import com.sanisidro.restaurante.core.email.service.EmailService;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.notifications.dto.EmailVerificationEvent;
import com.sanisidro.restaurante.features.notifications.dto.NotifiableEvent;
import com.sanisidro.restaurante.features.notifications.dto.OrderNotificationEvent;
import com.sanisidro.restaurante.features.notifications.dto.ReservationNotificationEvent;
import com.sanisidro.restaurante.features.notifications.enums.NotificationStatus;
import com.sanisidro.restaurante.features.notifications.metrics.NotificationMetricsService;
import com.sanisidro.restaurante.features.notifications.model.EmailNotification;
import com.sanisidro.restaurante.features.notifications.repository.EmailNotificationRepository;
import com.sanisidro.restaurante.features.notifications.templates.EmailTemplateBuilder;
import com.sanisidro.restaurante.features.notifications.templates.EmailTemplateBuilder.OrderProduct;
import com.sanisidro.restaurante.features.notifications.templates.EmailVerificationTemplateBuilder;
import com.sanisidro.restaurante.features.notifications.templates.ReservationEmailTemplateBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service("EMAIL")
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationChannel {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final EmailService emailService;
    private final EmailNotificationRepository emailNotificationRepository;
    private final UserRepository userRepository;
    private final NotificationMetricsService notificationMetricsService;

    @Override
    public void send(NotifiableEvent event) {
        User user = resolveUser(event);
        String recipient = resolveRecipient(event, user);

        if (recipient == null) {
            handleFailed(event, user, "no_recipient");
            return;
        }

        if (isInvalidContent(event)) {
            handleFailed(event, user, "invalid_content");
            return;
        }

        String emailHtml = buildEmailHtml(event, user);

        EmailNotification email = EmailNotification.builder()
                .toAddress(recipient)
                .subject(event.getSubject())
                .body(emailHtml)
                .sentAt(LocalDateTime.now())
                .status(NotificationStatus.PENDING)
                .user(user)
                .build();

        emailNotificationRepository.save(email);
        sendWithRetries(email, recipient, emailHtml);
    }

    private User resolveUser(NotifiableEvent event) {
        if (event.getUserId() != null) {
            return userRepository.findById(event.getUserId()).orElse(null);
        }
        return null;
    }

    private String resolveRecipient(NotifiableEvent event, User user) {
        if (event.getRecipient() != null && !event.getRecipient().isBlank()) {
            return event.getRecipient();
        }
        return user != null ? user.getEmail() : null;
    }

    private boolean isInvalidContent(NotifiableEvent event) {
        return event.getSubject() == null || event.getSubject().isBlank() ||
                event.getMessage() == null || event.getMessage().isBlank();
    }

    private void handleFailed(NotifiableEvent event, User user, String reason) {
        log.warn("⚠️ Notificación fallida: {} para userId={}", reason, event.getUserId());
        notificationMetricsService.incrementFailed("EMAIL", reason);

        EmailNotification failedEmail = EmailNotification.builder()
                .toAddress(event.getRecipient())
                .subject(event.getSubject())
                .body(event.getMessage())
                .sentAt(LocalDateTime.now())
                .status(NotificationStatus.FAILED)
                .user(user)
                .build();

        emailNotificationRepository.save(failedEmail);
    }

    private String buildEmailHtml(NotifiableEvent event, User user) {
        String recipientName = user != null ? user.getFullName() : "Cliente";

        if (event instanceof OrderNotificationEvent orderEvent) {
            List<OrderProduct> products = orderEvent.getProducts() != null
                    ? orderEvent.getProducts().stream()
                    .map(p -> new OrderProduct(p.getName(), p.getUnitPrice(), p.getQuantity()))
                    .collect(Collectors.toList())
                    : List.of();

            return EmailTemplateBuilder.buildOrderConfirmationEmail(
                    recipientName,
                    orderEvent.getOrderId(),
                    products,
                    orderEvent.getTotal(),
                    orderEvent.getOrderDate(),
                    orderEvent.getActionUrl() != null ? orderEvent.getActionUrl() : "#",
                    orderEvent.getMessage()
            );

        } else if (event instanceof ReservationNotificationEvent reservationEvent) {
            return ReservationEmailTemplateBuilder.buildReservationConfirmationEmail(
                    reservationEvent,
                    frontendUrl
            );

        } else if (event instanceof EmailVerificationEvent verificationEvent) {
            String actionUrl = verificationEvent.getActionUrl();
            return EmailVerificationTemplateBuilder.buildVerificationEmail(verificationEvent, recipientName, actionUrl);
        } else {
            return EmailTemplateBuilder.buildPromotionEmail(
                    event.getSubject(),
                    event.getMessage(),
                    event.getActionUrl() != null ? event.getActionUrl() : "#"
            );
        }
    }

    private void sendWithRetries(EmailNotification email, String recipient, String emailHtml) {
        int attempts = 0;
        boolean sent = false;

        while (attempts < 3 && !sent) {
            try {
                emailService.sendEmail(EmailMessageRequest.builder()
                        .toAddress(recipient)
                        .subject(email.getSubject())
                        .body(emailHtml)
                        .build());
                sent = true;
                email.setStatus(NotificationStatus.SENT);
                notificationMetricsService.incrementSent("EMAIL");
                log.info("📧 Notificación enviada a {} en intento #{}", recipient, attempts + 1);
            } catch (Exception ex) {
                attempts++;
                log.warn("⚠️ Intento #{} fallido para {}: {}", attempts, recipient, ex.getMessage());
                if (attempts == 3) {
                    email.setStatus(NotificationStatus.FAILED);
                    notificationMetricsService.incrementFailed("EMAIL", "smtp_error");
                    log.error("❌ No se pudo enviar el correo a {} después de 3 intentos", recipient, ex);
                }
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }

        emailNotificationRepository.save(email);
    }
}
