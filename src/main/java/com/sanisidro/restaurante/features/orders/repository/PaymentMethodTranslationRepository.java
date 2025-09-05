package com.sanisidro.restaurante.features.orders.repository;

import com.sanisidro.restaurante.features.orders.model.PaymentMethod;
import com.sanisidro.restaurante.features.orders.model.PaymentMethodTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentMethodTranslationRepository extends JpaRepository<PaymentMethodTranslation, Long> {
    Optional<PaymentMethodTranslation> findByPaymentMethodAndLang(PaymentMethod paymentMethod, String lang);
}
