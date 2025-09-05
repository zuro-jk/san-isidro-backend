package com.sanisidro.restaurante.features.orders.model;

import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.features.orders.enums.IncidentStatus;
import com.sanisidro.restaurante.features.orders.enums.IncidentType;
import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.suppliers.model.Supplier;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incident_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private IncidentType type;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private IncidentStatus status;
}
