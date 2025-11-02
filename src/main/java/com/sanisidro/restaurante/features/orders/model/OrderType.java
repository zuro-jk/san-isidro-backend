package com.sanisidro.restaurante.features.orders.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @OneToMany(mappedBy = "orderType", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<OrderTypeTranslation> translations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "orderType", fetch = FetchType.LAZY)
    @OrderBy("stepOrder ASC")
    private List<OrderTypeStatusFlow> statusFlow;
}
