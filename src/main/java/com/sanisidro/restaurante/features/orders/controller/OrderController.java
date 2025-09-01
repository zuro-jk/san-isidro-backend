package com.sanisidro.restaurante.features.orders.controller;

import com.sanisidro.restaurante.features.orders.dto.order.response.OrderResponse;
import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

//    @GetMapping()
//    public ResponseEntity<List<OrderResponse>> getAllOrders() {
//        return ResponseEntity.ok(orderService.getOrders());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<OrderResponse> getOrder(
//            @PathVariable Long id
//    ) {
//        return ResponseEntity.ok(orderService.getOrder(id));
//    }



}
