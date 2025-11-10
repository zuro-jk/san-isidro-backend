package com.sanisidro.restaurante.features.feedbackloyalty.models;


import com.sanisidro.restaurante.features.customers.enums.LoyaltyRuleType;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "loyalty_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer points;

    private Double minPurchaseAmount;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private boolean perPerson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoyaltyRuleType type;
}