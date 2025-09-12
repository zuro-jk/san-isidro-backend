package com.sanisidro.restaurante.features.customers.model;


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

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer points;

    private Double minPurchaseAmount;

    private Boolean active = true;

    @Column(nullable = false)
    private boolean perPerson;

}
