package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.orders.dto.ordertype.request.OrderTypeRequest;
import com.sanisidro.restaurante.features.orders.dto.ordertype.response.OrderTypeResponse;
import com.sanisidro.restaurante.features.orders.model.OrderType;
import com.sanisidro.restaurante.features.orders.model.OrderTypeTranslation;
import com.sanisidro.restaurante.features.orders.repository.OrderTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderTypeService {

    private final OrderTypeRepository orderTypeRepository;

    public List<OrderTypeResponse> getAll(String lang) {
        return orderTypeRepository.findAll().stream()
                .map(type -> mapToResponse(type, lang))
                .collect(Collectors.toList());
    }

    public OrderTypeResponse getById(Long id, String lang) {
        return orderTypeRepository.findById(id)
                .map(type -> mapToResponse(type, lang))
                .orElseThrow(() -> new EntityNotFoundException("Tipo de orden no encontrado con id: " + id));
    }

    public OrderTypeResponse create(OrderTypeRequest request) {
        OrderType type = OrderType.builder()
                .code(request.getCode())
                .build();

        OrderTypeTranslation translation = OrderTypeTranslation.builder()
                .orderType(type)
                .lang(request.getLang())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        type.getTranslations().add(translation);

        return mapToResponse(orderTypeRepository.save(type), request.getLang());
    }

    public OrderTypeResponse update(Long id, OrderTypeRequest request) {
        OrderType type = orderTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de orden no encontrado con id: " + id));

        type.setCode(request.getCode());

        OrderTypeTranslation translation = type.getTranslations().stream()
                .filter(t -> t.getLang().equals(request.getLang()))
                .findFirst()
                .orElse(OrderTypeTranslation.builder()
                        .orderType(type)
                        .lang(request.getLang())
                        .build());

        translation.setName(request.getName());
        translation.setDescription(request.getDescription());
        type.getTranslations().add(translation);

        return mapToResponse(orderTypeRepository.save(type), request.getLang());
    }

    public void delete(Long id) {
        if (!orderTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("Tipo de orden no encontrado con id: " + id);
        }
        orderTypeRepository.deleteById(id);
    }

    private OrderTypeResponse mapToResponse(OrderType type, String lang) {
        OrderTypeTranslation translation = type.getTranslations().stream()
                .filter(t -> t.getLang().equals(lang))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Traducción no encontrada para el idioma: " + lang));

        return OrderTypeResponse.builder()
                .id(type.getId())
                .code(type.getCode())
                .name(translation.getName())
                .description(translation.getDescription())
                .lang(translation.getLang())
                .build();
    }

}
