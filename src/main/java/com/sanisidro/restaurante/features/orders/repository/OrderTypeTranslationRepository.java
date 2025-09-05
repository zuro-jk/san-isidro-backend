package com.sanisidro.restaurante.features.orders.repository;

import com.sanisidro.restaurante.features.orders.model.OrderType;
import com.sanisidro.restaurante.features.orders.model.OrderTypeTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderTypeTranslationRepository extends JpaRepository<OrderTypeTranslation, Long> {
    Optional<OrderTypeTranslation> findByOrderTypeAndLang(OrderType orderType, String lang);
}