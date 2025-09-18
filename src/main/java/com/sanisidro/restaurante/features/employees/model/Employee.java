package com.sanisidro.restaurante.features.employees.model;

import com.sanisidro.restaurante.core.model.Auditable;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.features.employees.enums.EmploymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "employee_id"))
public class Employee extends Auditable {

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(name = "salary", precision = 10, scale = 2)
    private BigDecimal salary;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EmploymentStatus status;

}
