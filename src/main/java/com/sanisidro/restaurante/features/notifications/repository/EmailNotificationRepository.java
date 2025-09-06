package com.sanisidro.restaurante.features.notifications.repository;

import com.sanisidro.restaurante.features.notifications.model.EmailNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {
}
