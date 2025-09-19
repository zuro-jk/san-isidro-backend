package com.sanisidro.restaurante.features.orders.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_method_translations",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"payment_method_id", "lang"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "translation_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "lang", nullable = false, length = 5)
    private String lang;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;
}