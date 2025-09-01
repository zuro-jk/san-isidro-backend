package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.orders.dto.order.response.OrderResponse;
import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

//    public List<OrderResponse> getOrders() {
//        return orderRepository.findAll();
//    }
//
//    private OrderResponse mapToResponse(Order order) {
//        return
//    }

}
