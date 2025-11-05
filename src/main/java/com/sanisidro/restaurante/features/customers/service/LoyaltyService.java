package com.sanisidro.restaurante.features.customers.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.model.LoyaltyRule;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.customers.repository.LoyaltyRuleRepository;
import com.sanisidro.restaurante.features.customers.repository.PointsHistoryRepository;
import com.sanisidro.restaurante.features.customers.repository.RewardRepository;
import com.sanisidro.restaurante.features.feedbackloyalty.dto.response.LoyalCustomerResponse;
import com.sanisidro.restaurante.features.feedbackloyalty.dto.response.RewardResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyRuleRepository loyaltyRuleRepository;
    private final PointsHistoryRepository pointsHistoryRepository;
    private final CustomerRepository customerRepository;
    private final RewardRepository rewardRepository;

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

    @Transactional(readOnly = true)
    public int getTotalPointsAccumulated() {
        return pointsHistoryRepository.getTotalPointsAccumulated();
    }

    @Transactional(readOnly = true)
    public int getTotalPointsRedeemed() {
        return pointsHistoryRepository.getTotalPointsRedeemed();
    }

    public List<LoyalCustomerResponse> getTopLoyalCustomers(int limit) {
        return customerRepository.findTopCustomersByPoints(limit)
                .stream()
                .map(c -> new LoyalCustomerResponse(
                        c.getId(),
                        c.getUser().getFullName(),
                        c.getUser().getEmail(),
                        c.getPoints()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RewardResponse> getAvailableRewards() {
        return rewardRepository.findByActiveTrue()
                .stream()
                .map(r -> RewardResponse.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .requiredPoints(r.getRequiredPoints())
                        .build())
                .toList();
    }
}