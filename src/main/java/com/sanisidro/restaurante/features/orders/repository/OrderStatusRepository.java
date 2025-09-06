package com.sanisidro.restaurante.features.orders.repository;

import com.sanisidro.restaurante.features.orders.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {
}
