package com.sanisidro.restaurante.features.employees.dto.employee.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sanisidro.restaurante.features.employees.enums.EmploymentStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")
    private String username;

    @NotNull(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Size(max = 100)
    private String email;

    private String password;

    @NotNull(message = "El nombre es obligatorio")
    @Size(max = 50)
    private String firstName;

    @NotNull(message = "El apellido es obligatorio")
    @Size(max = 50)
    private String lastName;

    private String phone;

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