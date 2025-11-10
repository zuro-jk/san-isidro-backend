package com.sanisidro.restaurante.features.cashclosing.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.sanisidro.restaurante.features.employees.model.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cash_closing_sessions")
public class CashClosingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime closingTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal openingBalance;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, BigDecimal> salesByPaymentMethod;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal expectedCash;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal countedCash;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal difference;

    private String notes;
}
