package com.sanisidro.restaurante.features.restaurant.dto.table.request;


import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableRequest {

    @NotBlank(message = "El nombre de la mesa es obligatorio")
    @Size(max = 50, message = "El nombre no debe exceder los 50 caracteres")
    private String name;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad mínima es 1 persona")
    @Max(value = 50, message = "La capacidad máxima es 50 personas")
    private Integer capacity;

    @Size(max = 255, message = "La descripción no debe exceder los 255 caracteres")
    private String description;
}
