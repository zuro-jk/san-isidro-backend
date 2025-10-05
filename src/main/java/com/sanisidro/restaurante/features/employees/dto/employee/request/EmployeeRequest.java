package com.sanisidro.restaurante.features.employees.dto.employee.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sanisidro.restaurante.features.employees.enums.EmploymentStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Long userId;

    @NotNull(message = "El puesto es obligatorio")
    private Long positionId;

    @NotNull(message = "El salario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El salario debe ser mayor a 0")
    private BigDecimal salary;

    @NotNull(message = "La fecha de contratación es obligatoria")
    private LocalDate hireDate;

    @NotNull(message = "El estado es obligatorio")
    private EmploymentStatus status;
}