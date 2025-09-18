package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.core.exceptions.InvalidReservationException;
import com.sanisidro.restaurante.core.exceptions.ResourceNotFoundException;
import com.sanisidro.restaurante.features.customers.dto.loyaltyrule.request.LoyaltyRuleRequest;
import com.sanisidro.restaurante.features.customers.dto.loyaltyrule.response.LoyaltyRuleResponse;
import com.sanisidro.restaurante.features.customers.enums.LoyaltyRuleType;
import com.sanisidro.restaurante.features.customers.model.LoyaltyRule;
import com.sanisidro.restaurante.features.customers.repository.LoyaltyRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoyaltyRuleService {

    private final LoyaltyRuleRepository loyaltyRuleRepository;

    /* -------------------- READ -------------------- */
    public LoyaltyRuleResponse getRule(Long id) {
        LoyaltyRule rule = loyaltyRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regla de fidelidad no encontrada"));
        return mapToResponse(rule);
    }

    public List<LoyaltyRuleResponse> getAllRules() {
        return loyaltyRuleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /* -------------------- CREATE -------------------- */
    public LoyaltyRuleResponse createRule(LoyaltyRuleRequest request) {
        validateRequest(request);

        LoyaltyRule rule = LoyaltyRule.builder()
                .name(request.getName())
                .points(request.getPoints())
                .minPurchaseAmount(request.getMinPurchaseAmount())
                .active(request.getActive())
                .perPerson(request.getPerPerson())
                .type(request.getType())
                .build();

        LoyaltyRule saved = loyaltyRuleRepository.save(rule);
        return mapToResponse(saved);
    }

    /* -------------------- UPDATE -------------------- */
    public LoyaltyRuleResponse updateRule(Long id, LoyaltyRuleRequest request) {
        LoyaltyRule rule = loyaltyRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regla de fidelidad no encontrada"));

        validateRequest(request);

        rule.setName(request.getName());
        rule.setPoints(request.getPoints());
        rule.setMinPurchaseAmount(request.getMinPurchaseAmount());
        rule.setActive(request.getActive());
        rule.setPerPerson(request.getPerPerson());
        rule.setType(request.getType());

        LoyaltyRule updated = loyaltyRuleRepository.save(rule);
        return mapToResponse(updated);
    }

    /* -------------------- DELETE -------------------- */
    public void deleteRule(Long id) {
        if (!loyaltyRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Regla de fidelidad no encontrada");
        }
        loyaltyRuleRepository.deleteById(id);
    }

    /* -------------------- HELPERS -------------------- */
    private LoyaltyRuleResponse mapToResponse(LoyaltyRule rule) {
        return LoyaltyRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .points(rule.getPoints())
                .minPurchaseAmount(rule.getMinPurchaseAmount())
                .active(rule.getActive())
                .perPerson(rule.isPerPerson())
                .type(rule.getType())
                .build();
    }

    private void validateRequest(LoyaltyRuleRequest request) {
        // Validación de puntos
        if (request.getPoints() < 0) {
            throw new InvalidReservationException("Los puntos deben ser mayores o iguales a 0");
        }

        // Validación de minPurchaseAmount solo para PURCHASE
        if (request.getType() == LoyaltyRuleType.PURCHASE && (request.getMinPurchaseAmount() == null || request.getMinPurchaseAmount() <= 0)) {
            throw new InvalidReservationException("El monto mínimo de compra debe ser mayor a 0 para reglas de tipo PURCHASE");
        }

        if (request.getType() != LoyaltyRuleType.PURCHASE) {
            request.setMinPurchaseAmount(null); // limpiar si no aplica
        }
    }
}