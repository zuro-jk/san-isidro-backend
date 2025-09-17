package com.sanisidro.restaurante.features.products.model;

import com.sanisidro.restaurante.features.products.exceptions.InsufficientStockException;
import com.sanisidro.restaurante.features.products.exceptions.InvalidQuantityException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false, unique = true)
    private Ingredient ingredient;

    @Column(name = "current_stock", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentStock;

    @Column(name = "minimum_stock", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumStock;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void increaseStock(BigDecimal qty) {
        if (qty.compareTo(BigDecimal.ZERO) < 0) throw new InvalidQuantityException("Cantidad debe ser positiva");
        this.currentStock = this.currentStock.add(qty);
    }

    public void decreaseStock(BigDecimal qty) {
        if (qty.compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidQuantityException("Cantidad debe ser positiva");

        if (this.currentStock.compareTo(qty) < 0)
            throw new InsufficientStockException(
                    "Stock insuficiente. Disponible: " + this.currentStock + ", solicitado: " + qty
            );

        this.currentStock = this.currentStock.subtract(qty);
    }
}