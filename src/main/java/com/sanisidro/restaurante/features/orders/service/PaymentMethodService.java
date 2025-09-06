package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.orders.dto.paymentmethod.request.PaymentMethodRequest;
import com.sanisidro.restaurante.features.orders.dto.paymentmethod.resposne.PaymentMethodResponse;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodTranslationRepository translationRepository;

    public List<PaymentMethodResponse> getAll(String lang) {
        return paymentMethodRepository.findAll().stream()
                .map(method -> mapToResponse(method, lang))
                .toList();
    }

    public PaymentMethodResponse getById(Long id, String lang) {
        return paymentMethodRepository.findById(id)
                .map(method -> mapToResponse(method, lang))
                .orElseThrow(() -> new EntityNotFoundException("Método de pago no encontrado con id: " + id));
    }

    @Transactional
    public PaymentMethodResponse create(PaymentMethodRequest request) {
        PaymentMethod method = PaymentMethod.builder()
                .code(request.getCode())
                .translations(new LinkedHashSet<>())
                .build();

        method = paymentMethodRepository.save(method);

        PaymentMethodTranslation translation = PaymentMethodTranslation.builder()
                .paymentMethod(method)
                .lang(request.getLang() != null ? request.getLang() : "es")
                .name(request.getName())
                .description(request.getDescription())
                .build();

        translationRepository.save(translation);

        method.getTranslations().add(translation);

        return mapToResponse(method, translation.getLang());
    }

    @Transactional
    public PaymentMethodResponse update(Long id, PaymentMethodRequest request) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Método de pago no encontrado con id: " + id));

        method.setCode(request.getCode());
        if (method.getTranslations() == null) {
            method.setTranslations(new LinkedHashSet<>());
        }
        method = paymentMethodRepository.save(method);

        String lang = request.getLang() != null ? request.getLang() : "es";

        Optional<PaymentMethodTranslation> existingTranslation =
                translationRepository.findByPaymentMethodAndLang(method, lang);

        PaymentMethodTranslation translation;
        if (existingTranslation.isPresent()) {
            translation = existingTranslation.get();
            translation.setName(request.getName());
            translation.setDescription(request.getDescription());
        } else {
            translation = PaymentMethodTranslation.builder()
                    .paymentMethod(method)
                    .lang(lang)
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();

            translationRepository.save(translation);
            method.getTranslations().add(translation);
        }

        return mapToResponse(method, lang);
    }

    public void delete(Long id) {
        if (!paymentMethodRepository.existsById(id)) {
            throw new EntityNotFoundException("Método de pago no encontrado con id: " + id);
        }
        paymentMethodRepository.deleteById(id);
    }

    private PaymentMethodResponse mapToResponse(PaymentMethod method, String lang) {
        PaymentMethodTranslation translation = method.getTranslations().stream()
                .filter(t -> t.getLang().equals(lang))
                .findFirst()
                .orElse(method.getTranslations().stream().findFirst().orElse(null));

        return PaymentMethodResponse.builder()
                .id(method.getId())
                .code(method.getCode())
                .name(translation != null ? translation.getName() : null)
                .description(translation != null ? translation.getDescription() : null)
                .build();
    }

}
