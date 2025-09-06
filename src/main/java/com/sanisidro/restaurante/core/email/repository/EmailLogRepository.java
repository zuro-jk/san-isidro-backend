package com.sanisidro.restaurante.core.email.repository;

import com.sanisidro.restaurante.core.email.model.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    Optional<EmailLog> findFirstByToAddressAndSubjectOrderBySentAtDesc(String toAddress, String subject);
}