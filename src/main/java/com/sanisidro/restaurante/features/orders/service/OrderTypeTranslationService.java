package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.orders.dto.ordertypetranslation.request.OrderTypeTranslationRequest;
import com.sanisidro.restaurante.features.orders.dto.ordertypetranslation.response.OrderTypeTranslationResponse;
import com.sanisidro.restaurante.features.orders.model.OrderType;
import com.sanisidro.restaurante.features.orders.model.OrderTypeTranslation;
import com.sanisidro.restaurante.features.orders.repository.OrderTypeRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderTypeTranslationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderTypeTranslationService {

    private final OrderTypeTranslationRepository translationRepository;
    private final OrderTypeRepository orderTypeRepository;

    public List<OrderTypeTranslationResponse> getAllByOrderType(Long orderTypeId) {
        OrderType type = orderTypeRepository.findById(orderTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de orden no encontrado con id: " + orderTypeId));

        return type.getTranslations().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderTypeTranslationResponse getById(Long id) {
        OrderTypeTranslation translation = translationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Traducción no encontrada con id: " + id));
        return mapToResponse(translation);
    }

    public OrderTypeTranslationResponse create(OrderTypeTranslationRequest request) {
        OrderType type = orderTypeRepository.findById(request.getOrderTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de orden no encontrado con id: " + request.getOrderTypeId()));

        translationRepository.findByOrderTypeAndLang(type, request.getLang())
                .ifPresent(t -> { throw new IllegalArgumentException("La traducción ya existe para este idioma"); });

        OrderTypeTranslation translation = OrderTypeTranslation.builder()
                .orderType(type)
                .lang(request.getLang())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return mapToResponse(translationRepository.save(translation));
    }

    public OrderTypeTranslationResponse update(Long id, OrderTypeTranslationRequest request) {
        OrderTypeTranslation translation = translationRepository.findById(id)
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

    private OrderTypeTranslationResponse mapToResponse(OrderTypeTranslation translation) {
        return OrderTypeTranslationResponse.builder()
                .id(translation.getId())
                .orderTypeId(translation.getOrderType().getId())
                .lang(translation.getLang())
                .name(translation.getName())
                .description(translation.getDescription())
                .build();
    }

}
