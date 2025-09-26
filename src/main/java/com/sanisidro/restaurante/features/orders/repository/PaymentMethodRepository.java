package com.sanisidro.restaurante.features.orders.repository;

import com.sanisidro.restaurante.features.orders.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    Optional<PaymentMethod> findByCodeAndProvider(String code, String provider);

}
