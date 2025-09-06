package com.sanisidro.restaurante.features.orders.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_type_translations",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"order_type_id", "lang"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderTypeTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "translation_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_type_id", nullable = false)
    private OrderType orderType;

    @Column(name = "lang", nullable = false, length = 5)
    private String lang;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;
}
