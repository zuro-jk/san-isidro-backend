package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.orders.dto.ordertype.request.OrderTypeRequest;
import com.sanisidro.restaurante.features.orders.dto.ordertype.response.OrderTypeResponse;
import com.sanisidro.restaurante.features.orders.model.OrderType;
import com.sanisidro.restaurante.features.orders.repository.OrderTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderTypeService {

    private final OrderTypeRepository orderTypeRepository;

    public List<OrderTypeResponse> getAll() {
        return orderTypeRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderTypeResponse getById(Long id) {
        return orderTypeRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de orden no encontrado con id: " + id));
    }

    public OrderTypeResponse create(OrderTypeRequest request) {
        OrderType type = OrderType.builder()
                .name(request.getName())
                .build();
        return mapToResponse(orderTypeRepository.save(type));
    }

    public OrderTypeResponse update(Long id, OrderTypeRequest request) {
        OrderType type = orderTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de orden no encontrado con id: " + id));

        type.setName(request.getName());
        return mapToResponse(orderTypeRepository.save(type));
    }

    public void delete(Long id) {
        if (!orderTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("Tipo de orden no encontrado con id: " + id);
        }
        orderTypeRepository.deleteById(id);
    }

    private OrderTypeResponse mapToResponse(OrderType type) {
        return OrderTypeResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .build();
    }

}
