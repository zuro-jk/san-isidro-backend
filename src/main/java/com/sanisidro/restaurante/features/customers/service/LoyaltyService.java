package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.customers.repository.LoyaltyRuleRepository;
import com.sanisidro.restaurante.features.customers.repository.PointsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyRuleRepository loyaltyRuleRepository;

    /**
     * Calcula cuántos puntos deberían aplicarse según el evento.
     * No persiste nada.
     */
    public int calculatePoints(Customer customer, Double purchaseAmount, String event, int numberOfPeople) {
        return loyaltyRuleRepository.findByActiveTrue().stream()
                .filter(rule -> rule.getName().equalsIgnoreCase(event))
                .filter(rule -> rule.getMinPurchaseAmount() == null ||
                        (purchaseAmount != null && purchaseAmount >= rule.getMinPurchaseAmount()))
                .mapToInt(rule -> rule.isPerPerson() ? rule.getPoints() * numberOfPeople : rule.getPoints())
                .sum();
    }

    /**
     * Verifica si el evento de lealtad es válido según las reglas activas.
     */
    public boolean isValidEvent(String event) {
        return loyaltyRuleRepository.findByActiveTrue().stream()
                .anyMatch(rule -> rule.getName().equalsIgnoreCase(event));
    }

}
