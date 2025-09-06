package com.sanisidro.restaurante.core.email.service;

import com.sanisidro.restaurante.core.email.dto.request.EmailMessageRequest;
import com.sanisidro.restaurante.core.email.dto.resposne.EmailMessageResponse;
import com.sanisidro.restaurante.core.email.model.EmailLog;
import com.sanisidro.restaurante.core.email.repository.EmailLogRepository;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @PostConstruct
    public void init() {
        log.info("📨 Configuración de correo cargada:");
        log.info("   → Remitente (spring.mail.username): {}", senderEmail);
    }

    public EmailMessageResponse sendEmail(EmailMessageRequest request) {
        log.info("➡️ Intentando enviar correo");
        log.info("   → De: {}", senderEmail);
        log.info("   → Para: {}", request.getToAddress());
        log.info("   → Asunto: {}", request.getSubject());

        var previous = emailLogRepository.findFirstByToAddressAndSubjectOrderBySentAtDesc(
                request.getToAddress(), request.getSubject()
        );

        if (previous.isPresent() && "SENT".equals(previous.get().getStatus())) {
            return EmailMessageResponse.builder()
                    .success(false)
                    .message("El correo ya fue enviado previamente")
                    .build();
        }

        EmailLog logEntity = EmailLog.builder()
                .toAddress(request.getToAddress())
                .subject(request.getSubject())
                .body(request.getBody())
                .sentAt(LocalDateTime.now())
                .build();

        try {

            log.info("✉️ Enviando correo...");
            log.info("   → Remitente usado (from): {}", senderEmail);
            log.info("   → Destinatario (to): {}", request.getToAddress());


            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(request.getToAddress());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), true);

            mailSender.send(message);

            logEntity.setStatus("SENT");
            emailLogRepository.save(logEntity);

            log.info("✅ Correo enviado correctamente a {}", request.getToAddress());

            return EmailMessageResponse.builder()
                    .success(true)
                    .message("Correo enviado correctamente")
                    .build();

        } catch (MessagingException | MailException e) {
            log.error("❌ Error enviando correo a {}: {}", request.getToAddress(), e.getMessage(), e);

            logEntity.setStatus("FAILED");
            logEntity.setErrorMessage(e.getMessage());
            emailLogRepository.save(logEntity);

            return EmailMessageResponse.builder()
                    .success(false)
                    .message("Error al enviar correo: " + e.getMessage())
                    .build();
        }
    }

}
