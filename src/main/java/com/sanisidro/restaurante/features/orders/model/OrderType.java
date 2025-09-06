package com.sanisidro.restaurante.features.orders.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "order_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_type_id")
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @OneToMany(mappedBy = "orderType", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<OrderTypeTranslation> translations = new LinkedHashSet<>();
}
