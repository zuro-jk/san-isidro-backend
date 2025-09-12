package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.core.exceptions.ResourceNotFoundException;
import com.sanisidro.restaurante.features.customers.dto.loyaltyrule.request.LoyaltyRuleRequest;
import com.sanisidro.restaurante.features.customers.dto.loyaltyrule.response.LoyaltyRuleResponse;
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


    public LoyaltyRuleResponse createRule(LoyaltyRuleRequest request) {
        LoyaltyRule rule = new LoyaltyRule();
        rule.setName(request.getName());
        rule.setPoints(request.getPoints());
        rule.setMinPurchaseAmount(request.getMinPurchaseAmount());
        rule.setActive(request.getActive());

        LoyaltyRule saved = loyaltyRuleRepository.save(rule);
        return mapToResponse(saved);
    }

    public LoyaltyRuleResponse updateRule(Long id, LoyaltyRuleRequest request) {
        LoyaltyRule rule = loyaltyRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regla de fidelidad no encontrada"));

        rule.setName(request.getName());
        rule.setPoints(request.getPoints());
        rule.setMinPurchaseAmount(request.getMinPurchaseAmount());
        rule.setActive(request.getActive());

        LoyaltyRule updated = loyaltyRuleRepository.save(rule);
        return mapToResponse(updated);
    }


    public void deleteRule(Long id) {
        if (!loyaltyRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Regla de fidelidad no encontrada");
        }
        loyaltyRuleRepository.deleteById(id);
    }

    private LoyaltyRuleResponse mapToResponse(LoyaltyRule rule) {
        return new LoyaltyRuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getPoints(),
                rule.getMinPurchaseAmount(),
                rule.getActive(),
                rule.isPerPerson()
        );
    }

}
