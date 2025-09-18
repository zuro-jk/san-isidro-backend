package com.sanisidro.restaurante.features.employees.dto.employee.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionRequest {

    @NotBlank(message = "El nombre del puesto es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder los 100 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción no debe exceder los 255 caracteres")
    private String description;

    @NotNull(message = "Debe proporcionar al menos un rol")
    @NotEmpty(message = "La lista de roles no puede estar vacía")
    private Set<Long> roleIds;
}