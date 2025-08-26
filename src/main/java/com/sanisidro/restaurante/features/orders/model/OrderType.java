package com.sanisidro.restaurante.features.orders.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "name", nullable = false)
    private String name;
}
