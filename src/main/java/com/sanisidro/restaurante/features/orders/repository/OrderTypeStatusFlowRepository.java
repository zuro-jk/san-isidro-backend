package com.sanisidro.restaurante.features.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanisidro.restaurante.features.orders.model.OrderTypeStatusFlow;

public interface OrderTypeStatusFlowRepository extends JpaRepository<OrderTypeStatusFlow, Long> {

}
