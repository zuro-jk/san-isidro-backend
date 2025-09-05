package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.orders.dto.orderstatus.request.OrderStatusRequest;
import com.sanisidro.restaurante.features.orders.dto.orderstatus.response.OrderStatusResponse;
import com.sanisidro.restaurante.features.orders.model.OrderStatus;
import com.sanisidro.restaurante.features.orders.repository.OrderStatusRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final OrderStatusRepository orderStatusRepository;


    public List<OrderStatusResponse> getAll() {
        return orderStatusRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderStatusResponse getById(Long id) {
        return orderStatusRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Estado de orden no encontrado con id: " + id));
    }

    public OrderStatusResponse create(OrderStatusRequest request) {
        OrderStatus status = OrderStatus.builder()
                .name(request.getName())
                .build();
        return mapToResponse(orderStatusRepository.save(status));
    }

    public OrderStatusResponse update(Long id, OrderStatusRequest request) {
        OrderStatus status = orderStatusRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estado de orden no encontrado con id: " + id));

        status.setName(request.getName());
        return mapToResponse(orderStatusRepository.save(status));
    }

    public void delete(Long id) {
        if (!orderStatusRepository.existsById(id)) {
            throw new EntityNotFoundException("Estado de orden no encontrado con id: " + id);
        }
        orderStatusRepository.deleteById(id);
    }

    private OrderStatusResponse mapToResponse(OrderStatus status) {
        return OrderStatusResponse.builder()
                .id(status.getId())
                .name(status.getName())
                .build();
    }

}
