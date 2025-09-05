package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.orders.dto.orderstatustranslation.request.OrderStatusTranslationRequest;
import com.sanisidro.restaurante.features.orders.dto.orderstatustranslation.response.OrderStatusTranslationResponse;
import com.sanisidro.restaurante.features.orders.model.OrderStatus;
import com.sanisidro.restaurante.features.orders.model.OrderStatusTranslation;
import com.sanisidro.restaurante.features.orders.repository.OrderStatusRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderStatusTranslationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderStatusTranslationService {

    private final OrderStatusTranslationRepository translationRepository;
    private final OrderStatusRepository orderStatusRepository;

    public List<OrderStatusTranslationResponse> getAllByOrderStatus(Long orderStatusId) {
        OrderStatus status = orderStatusRepository.findById(orderStatusId)
                .orElseThrow(() -> new EntityNotFoundException("Estado de orden no encontrado con id: " + orderStatusId));

        return status.getTranslations().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderStatusTranslationResponse getById(Long id) {
        OrderStatusTranslation translation = translationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Traducción no encontrada con id: " + id));
        return mapToResponse(translation);
    }

    public OrderStatusTranslationResponse create(OrderStatusTranslationRequest request) {
        OrderStatus status = orderStatusRepository.findById(request.getOrderStatusId())
                .orElseThrow(() -> new EntityNotFoundException("Estado de orden no encontrado con id: " + request.getOrderStatusId()));

        translationRepository.findByOrderStatusAndLang(status, request.getLang())
                .ifPresent(t -> { throw new IllegalArgumentException("La traducción ya existe para este idioma"); });

        OrderStatusTranslation translation = OrderStatusTranslation.builder()
                .orderStatus(status)
                .lang(request.getLang())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return mapToResponse(translationRepository.save(translation));
    }

    public OrderStatusTranslationResponse update(Long id, OrderStatusTranslationRequest request) {
        OrderStatusTranslation translation = translationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Traducción no encontrada con id: " + id));

        translation.setName(request.getName());
        translation.setDescription(request.getDescription());

        return mapToResponse(translationRepository.save(translation));
    }

    public void delete(Long id) {
        if (!translationRepository.existsById(id)) {
            throw new EntityNotFoundException("Traducción no encontrada con id: " + id);
        }
        translationRepository.deleteById(id);
    }

    private OrderStatusTranslationResponse mapToResponse(OrderStatusTranslation translation) {
        return OrderStatusTranslationResponse.builder()
                .id(translation.getId())
                .orderStatusId(translation.getOrderStatus().getId())
                .lang(translation.getLang())
                .name(translation.getName())
                .description(translation.getDescription())
                .build();
    }

}
