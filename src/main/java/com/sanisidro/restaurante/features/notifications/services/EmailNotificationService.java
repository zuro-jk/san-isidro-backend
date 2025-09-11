package com.sanisidro.restaurante.features.notifications.services;

import com.sanisidro.restaurante.core.email.dto.request.EmailMessageRequest;
import com.sanisidro.restaurante.core.email.service.EmailService;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.notifications.dto.NotificationEvent;
import com.sanisidro.restaurante.features.notifications.enums.NotificationStatus;
import com.sanisidro.restaurante.features.notifications.metrics.NotificationMetricsService;
import com.sanisidro.restaurante.features.notifications.model.EmailNotification;
import com.sanisidro.restaurante.features.notifications.repository.EmailNotificationRepository;
import com.sanisidro.restaurante.features.notifications.templates.EmailTemplateBuilder;
import com.sanisidro.restaurante.features.notifications.templates.EmailTemplateBuilder.OrderProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service("EMAIL")
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationChannel {

    private final EmailService emailService;
    private final EmailNotificationRepository emailNotificationRepository;
    private final UserRepository userRepository;
    private final NotificationMetricsService notificationMetricsService;

    @Override
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
            notificationMetricsService.incrementFailed("EMAIL", "no_recipient");
            saveFailedEmail(event, user);
            return;
        }

        if (event.getSubject() == null || event.getSubject().isBlank() ||
                event.getMessage() == null || event.getMessage().isBlank()) {
            log.warn("⚠️ Notificación fallida: asunto o cuerpo vacío para userId={}", userId);
            notificationMetricsService.incrementFailed("EMAIL", "invalid_content");
            saveFailedEmail(event, user);
            return;
        }

        String emailHtml;
        if (event.getProducts() != null && !event.getProducts().isEmpty()) {
            List<OrderProduct> products = event.getProducts().stream()
                    .map(p -> new OrderProduct(p.getName(), p.getUnitPrice(), p.getQuantity()))
                    .collect(Collectors.toList());

            emailHtml = EmailTemplateBuilder.buildOrderConfirmationEmail(
                    user != null ? user.getFullName() : "Cliente",
                    event.getOrderId(),
                    products,
                    event.getTotal(),
                    event.getOrderDate(),
                    event.getActionUrl() != null ? event.getActionUrl() : "#",
                    event.getLogoUrl()
            );
        } else {
            emailHtml = EmailTemplateBuilder.buildPromotionEmail(
                    event.getSubject(),
                    event.getMessage(),
                    event.getActionUrl() != null ? event.getActionUrl() : "#"
            );
        }

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

    private void sendWithRetries(EmailNotification email, String recipient, String emailHtml) {
        try {
            EmailMessageRequest request = EmailMessageRequest.builder()
                    .toAddress(recipient)
                    .subject(email.getSubject())
                    .body(emailHtml)
                    .build();

            int attempts = 0;
            boolean sent = false;
            while (attempts < 3 && !sent) {
                try {
                    emailService.sendEmail(request);
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
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Interrupción en reintentos de email para {}", recipient, e);
            email.setStatus(NotificationStatus.FAILED);
            notificationMetricsService.incrementFailed("EMAIL", "interrupted");
        } catch (Exception e) {
            log.error("❌ Error inesperado al enviar email a {}: {}", recipient, e.getMessage(), e);
            email.setStatus(NotificationStatus.FAILED);
            notificationMetricsService.incrementFailed("EMAIL", "unexpected_error");
        } finally {
            emailNotificationRepository.save(email);
        }
    }

    private void saveFailedEmail(NotificationEvent event, User user) {
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
}
