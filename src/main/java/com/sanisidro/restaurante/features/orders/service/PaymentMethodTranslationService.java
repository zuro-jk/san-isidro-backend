package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.orders.dto.paymentmethodtranslation.request.PaymentMethodTranslationRequest;
import com.sanisidro.restaurante.features.orders.dto.paymentmethodtranslation.response.PaymentMethodTranslationResponse;
import com.sanisidro.restaurante.features.orders.model.PaymentMethod;
import com.sanisidro.restaurante.features.orders.model.PaymentMethodTranslation;
import com.sanisidro.restaurante.features.orders.repository.PaymentMethodRepository;
import com.sanisidro.restaurante.features.orders.repository.PaymentMethodTranslationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodTranslationService {

    private final PaymentMethodTranslationRepository translationRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    public List<PaymentMethodTranslationResponse> getAll() {
        return translationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PaymentMethodTranslationResponse getById(Long id) {
        PaymentMethodTranslation translation = translationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Traducción no encontrada con id: " + id));
        return mapToResponse(translation);
    }

    @Transactional
    public PaymentMethodTranslationResponse create(PaymentMethodTranslationRequest request) {
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Método de pago no encontrado con id: " + request.getPaymentMethodId()));

        translationRepository.findByPaymentMethodAndLang(method, request.getLang())
                .ifPresent(t -> { throw new IllegalArgumentException("Ya existe una traducción para este idioma"); });

        PaymentMethodTranslation translation = PaymentMethodTranslation.builder()
                .paymentMethod(method)
                .lang(request.getLang())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        translationRepository.save(translation);

        if (method.getTranslations() == null) {
            method.setTranslations(new LinkedHashSet<>());
        }
        method.getTranslations().add(translation);

        return mapToResponse(translation);
    }

    @Transactional
    public PaymentMethodTranslationResponse update(Long id, PaymentMethodTranslationRequest request) {
        PaymentMethodTranslation translation = translationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Traducción no encontrada con id: " + id));

        translation.setLang(request.getLang());
        translation.setName(request.getName());
        translation.setDescription(request.getDescription());

        translationRepository.save(translation);

        return mapToResponse(translation);
    }

    public void delete(Long id) {
        if (!translationRepository.existsById(id)) {
            throw new EntityNotFoundException("Traducción no encontrada con id: " + id);
        }
        translationRepository.deleteById(id);
    }

    private PaymentMethodTranslationResponse mapToResponse(PaymentMethodTranslation translation) {
        return PaymentMethodTranslationResponse.builder()
                .id(translation.getId())
                .paymentMethodId(translation.getPaymentMethod().getId())
                .lang(translation.getLang())
                .name(translation.getName())
                .description(translation.getDescription())
                .build();
    }

}
