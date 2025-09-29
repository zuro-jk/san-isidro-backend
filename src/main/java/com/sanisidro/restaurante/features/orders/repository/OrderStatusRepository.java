package com.sanisidro.restaurante.features.orders.repository;

import com.sanisidro.restaurante.features.orders.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {
    Optional<OrderStatus> findByCode(String code);
}
