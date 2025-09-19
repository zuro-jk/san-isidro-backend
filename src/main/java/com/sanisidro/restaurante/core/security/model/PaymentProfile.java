package com.sanisidro.restaurante.core.security.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String docType;
    private String docNumber;
    private String phone;
    private String areaCode;
    private String street;
    private String city;
    private String zipCode;
}
