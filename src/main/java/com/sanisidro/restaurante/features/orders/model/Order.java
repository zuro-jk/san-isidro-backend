package com.sanisidro.restaurante.features.orders.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.restaurant.model.Store;
import com.sanisidro.restaurante.features.restaurant.model.TableEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "delivery_street")
    private String deliveryStreet;

    @Column(name = "delivery_reference")
    private String deliveryReference;

    @Column(name = "delivery_city")
    private String deliveryCity;

    @Column(name = "delivery_instructions")
    private String deliveryInstructions;

    @Column(name = "delivery_province")
    private String deliveryProvince;

    @Column(name = "delivery_zip_code")
    private String deliveryZipCode;

    @Column(name = "delivery_latitude")
    private Double deliveryLatitude;

    @Column(name = "delivery_longitude")
    private Double deliveryLongitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_store_id")
    private Store pickupStore;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private TableEntity table;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_status_id", nullable = false)
    private OrderStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_type_id", nullable = false)
    private OrderType type;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderDetail> details = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payment> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> documents = new LinkedHashSet<>();

}
