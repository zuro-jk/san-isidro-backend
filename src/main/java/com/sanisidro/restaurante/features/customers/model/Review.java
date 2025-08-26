package com.sanisidro.restaurante.features.customers.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

}
