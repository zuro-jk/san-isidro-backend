package com.sanisidro.restaurante.features.feedbackloyalty.dto.loyaltyrule.response;

import com.sanisidro.restaurante.features.customers.enums.LoyaltyRuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoyaltyRuleResponse {

    private Long id;
    private String name;
    private Integer points;
    private Double minPurchaseAmount;
    private Boolean active;
    private Boolean perPerson;
    private LoyaltyRuleType type;
}