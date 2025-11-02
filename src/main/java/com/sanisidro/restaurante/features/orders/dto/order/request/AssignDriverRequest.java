package com.sanisidro.restaurante.features.orders.dto.order.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AssignDriverRequest {
    @NotNull(message = "El ID del empleado (repartidor) es obligatorio")
    private Long employeeId;
}