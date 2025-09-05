package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.orders.dto.paymentmethod.request.PaymentMethodRequest;
import com.sanisidro.restaurante.features.orders.dto.paymentmethod.resposne.PaymentMethodResponse;
import com.sanisidro.restaurante.features.orders.model.PaymentMethod;
import com.sanisidro.restaurante.features.orders.repository.PaymentMethodRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    public List<PaymentMethodResponse> getAll() {
        return paymentMethodRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PaymentMethodResponse getById(Long id) {
        return paymentMethodRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Método de pago no encontrado con id: " + id));
    }

    public PaymentMethodResponse create(PaymentMethodRequest request) {
        PaymentMethod method = PaymentMethod.builder()
                .name(request.getName())
                .build();
        return mapToResponse(paymentMethodRepository.save(method));
    }

    public PaymentMethodResponse update(Long id, PaymentMethodRequest request) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Método de pago no encontrado con id: " + id));

        method.setName(request.getName());
        return mapToResponse(paymentMethodRepository.save(method));
    }

    public void delete(Long id) {
        if (!paymentMethodRepository.existsById(id)) {
            throw new EntityNotFoundException("Método de pago no encontrado con id: " + id);
        }
        paymentMethodRepository.deleteById(id);
    }

    private PaymentMethodResponse mapToResponse(PaymentMethod method) {
        return PaymentMethodResponse.builder()
                .id(method.getId())
                .name(method.getName())
                .build();
    }

}
