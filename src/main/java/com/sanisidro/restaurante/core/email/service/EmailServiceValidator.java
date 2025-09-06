package com.sanisidro.restaurante.core.email.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceValidator {

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${spring.mail.password}")
    private String senderPassword;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @PostConstruct
    public void validateMailCredentials() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props);
            Transport transport = session.getTransport("smtp");

            log.info("🔍 Probando login SMTP con correo: {}", senderEmail);
            log.info("🔍 Probando login SMTP con password: {}", senderPassword);

            transport.connect(mailHost, mailPort, senderEmail, senderPassword);
            log.info("✅ Credenciales de correo válidas: login exitoso con {}", senderEmail);

            transport.close();
        } catch (Exception e) {
            log.error("❌ Error validando credenciales de correo {}: {}", senderEmail, e.getMessage());
        }
    }
}
