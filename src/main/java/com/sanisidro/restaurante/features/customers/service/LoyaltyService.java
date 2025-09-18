package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.model.LoyaltyRule;
import com.sanisidro.restaurante.features.customers.repository.LoyaltyRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyRuleRepository loyaltyRuleRepository;

    /**
     * Calcula cuántos puntos deberían aplicarse según el evento.
     * No persiste nada.
     */
    public int calculatePoints(Customer customer, Double purchaseAmount, String eventName, int numberOfPeople) {
        List<LoyaltyRule> rules = loyaltyRuleRepository.findByActiveTrue();

        return rules.stream()
                .filter(rule -> rule.getName().equalsIgnoreCase(eventName))
                .filter(rule -> rule.getMinPurchaseAmount() == null ||
                        (purchaseAmount != null && purchaseAmount >= rule.getMinPurchaseAmount()))
                .mapToInt(rule -> rule.isPerPerson() ? rule.getPoints() * numberOfPeople : rule.getPoints())
                .sum();
    }

    /**
     * Verifica si el evento de lealtad es válido según las reglas activas.
     */
    public boolean isValidEvent(String eventName) {
        return loyaltyRuleRepository.findByActiveTrue().stream()
                .anyMatch(rule -> rule.getName().equalsIgnoreCase(eventName));
    }

    /**
     * Obtiene la regla activa por nombre.
     */
    public Optional<LoyaltyRule> getRuleByName(String eventName) {
        return loyaltyRuleRepository.findByActiveTrue().stream()
                .filter(rule -> rule.getName().equalsIgnoreCase(eventName))
                .findFirst();
    }
}