package com.sanisidro.restaurante.features.orders.repository;

import com.sanisidro.restaurante.features.orders.model.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTypeRepository extends JpaRepository<OrderType, Long> {

}
