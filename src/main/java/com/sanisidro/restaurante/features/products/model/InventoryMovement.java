package com.sanisidro.restaurante.features.products.model;

import com.sanisidro.restaurante.features.orders.enums.MovementSource;
import com.sanisidro.restaurante.features.products.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_movement_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Include
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private MovementType type;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 50, nullable = false)
    private MovementSource source;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (date == null) {
            date = now;
        }
        createdAt = now;
    }
}