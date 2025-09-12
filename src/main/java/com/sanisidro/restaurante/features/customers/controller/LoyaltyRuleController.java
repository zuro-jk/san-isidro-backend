package com.sanisidro.restaurante.features.customers.controller;


import com.sanisidro.restaurante.features.customers.dto.loyaltyrule.request.LoyaltyRuleRequest;
import com.sanisidro.restaurante.features.customers.dto.loyaltyrule.response.LoyaltyRuleResponse;
import com.sanisidro.restaurante.features.customers.model.LoyaltyRule;
import com.sanisidro.restaurante.features.customers.service.LoyaltyRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loyalty-rules")
@RequiredArgsConstructor
public class LoyaltyRuleController {

    private final LoyaltyRuleService loyaltyRuleService;

    @GetMapping
    public ResponseEntity<List<LoyaltyRuleResponse>> getAllRules() {
        return ResponseEntity.ok(loyaltyRuleService.getAllRules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoyaltyRuleResponse> getRule(@PathVariable Long id) {
        return ResponseEntity.ok(loyaltyRuleService.getRule(id));
    }

    @PostMapping
    public ResponseEntity<LoyaltyRuleResponse> createRule(@Valid @RequestBody LoyaltyRuleRequest request) {
        return new ResponseEntity<>(loyaltyRuleService.createRule(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoyaltyRuleResponse> updateRule(@PathVariable Long id, @Valid @RequestBody LoyaltyRuleRequest request) {
        return ResponseEntity.ok(loyaltyRuleService.updateRule(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        loyaltyRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}