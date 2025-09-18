package com.sanisidro.restaurante.features.employees.dto.employee.request;

import com.sanisidro.restaurante.features.employees.enums.EmploymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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