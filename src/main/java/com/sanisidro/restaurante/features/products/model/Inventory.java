package com.sanisidro.restaurante.features.products.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    @ToString.Include
    private Product product;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock;

    @Column(name = "minimum_stock", nullable = false)
    private Integer minimumStock;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void increaseStock(int qty) {
        if (qty < 0) throw new IllegalArgumentException("Cantidad debe ser positiva");
        this.currentStock += qty;
    }

    public void decreaseStock(int qty) {
        if (qty < 0) throw new IllegalArgumentException("Cantidad debe ser positiva");
        if (this.currentStock < qty) throw new IllegalStateException("Stock insuficiente");
        this.currentStock -= qty;
    }
}