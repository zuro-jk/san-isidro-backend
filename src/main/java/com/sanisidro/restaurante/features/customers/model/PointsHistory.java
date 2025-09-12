package com.sanisidro.restaurante.features.customers.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "points_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsHistory {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private String event;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
