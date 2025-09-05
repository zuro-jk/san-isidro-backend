package com.sanisidro.restaurante.features.orders.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_status_translations",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"order_status_id", "lang"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "translation_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_status_id", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "lang", nullable = false, length = 5)
    private String lang;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;
}
