package com.sanisidro.restaurante.features.orders.repository;

import com.sanisidro.restaurante.features.orders.model.OrderStatus;
import com.sanisidro.restaurante.features.orders.model.OrderStatusTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderStatusTranslationRepository extends JpaRepository<OrderStatusTranslation, Long> {
    Optional<OrderStatusTranslation> findByOrderStatusAndLang(OrderStatus orderStatus, String lang);
}