package com.sanisidro.restaurante.core.security.repository;

import com.sanisidro.restaurante.core.security.model.PaymentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentProfileRepository extends JpaRepository<PaymentProfile, Long> {
}
