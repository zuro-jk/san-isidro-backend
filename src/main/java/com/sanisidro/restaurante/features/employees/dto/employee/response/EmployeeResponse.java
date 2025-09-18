package com.sanisidro.restaurante.features.employees.dto.employee.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;

    private String positionName;
    private String positionDescription;

    private BigDecimal salary;
    private String status;
    private LocalDate hireDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}