package com.sanisidro.restaurante.features.employees.dto.employee.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Long userId;

    @NotBlank(message = "El cargo es obligatorio")
    @Size(max = 100, message = "El cargo no debe exceder los 100 caracteres")
    private String position;

    @NotNull(message = "El salario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El salario debe ser mayor a 0")
    private BigDecimal salary;
}
