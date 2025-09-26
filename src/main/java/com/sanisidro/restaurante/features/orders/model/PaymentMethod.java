package com.sanisidro.restaurante.features.orders.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "payment_methods",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"code", "provider"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_method_id")
    private Long id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @OneToMany(mappedBy = "paymentMethod", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PaymentMethodTranslation> translations = new LinkedHashSet<>();
}