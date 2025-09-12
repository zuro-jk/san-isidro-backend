package com.sanisidro.restaurante.features.employees.model;

import com.sanisidro.restaurante.core.model.Auditable;
import com.sanisidro.restaurante.core.security.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends Auditable {

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "position", nullable = false)
    private String position;


    private BigDecimal salary;

}
