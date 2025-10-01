package com.sanisidro.restaurante.features.orders.repository;

import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.orders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(Customer customer);
}
