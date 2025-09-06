package com.sanisidro.restaurante.features.customers.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotBlank
    @Column(name = "address", columnDefinition = "text", nullable = false)
    private String address;

    @Column(name = "reference", columnDefinition = "text")
    private String reference;

    public String getDescription() {
        if (reference != null && !reference.isBlank()) {
            return address + " (" + reference + ")";
        }
        return address;
    }
}
