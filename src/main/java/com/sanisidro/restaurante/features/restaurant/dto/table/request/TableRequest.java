package com.sanisidro.restaurante.features.restaurant.dto.table.request;


import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableRequest {

    @NotBlank(message = "El nombre de la mesa es obligatorio")
    @Size(max = 50, message = "El nombre no debe exceder los 50 caracteres")
    private String name;

    @NotNull(message = "La capacidad máxima es obligatoria")
    @Min(value = 1, message = "La capacidad mínima es 1 persona")
    @Max(value = 50, message = "La capacidad máxima es 50 personas")
    private Integer capacity;

    @NotNull(message = "La capacidad mínima es obligatoria")
    @Min(value = 1, message = "La capacidad mínima es 1 persona")
    private Integer minCapacity;

    @NotNull(message = "La capacidad óptima es obligatoria")
    @Min(value = 1, message = "La capacidad óptima debe ser al menos 1 persona")
    private Integer optimalCapacity;

    @NotNull(message = "La prioridad es obligatoria")
    @Min(value = 1, message = "Prioridad mínima es 1 (alta)")
    @Max(value = 3, message = "Prioridad máxima es 3 (baja)")
    private Integer priority;

    @Size(max = 255, message = "La descripción no debe exceder los 255 caracteres")
    private String description;

    @NotNull(message = "Hora de apertura es obligatoria")
    private LocalTime openTime;

    @NotNull(message = "Hora de cierre es obligatoria")
    private LocalTime closeTime;

    @NotNull(message = "Duración de reserva es obligatoria")
    @Min(value = 5, message = "Duración mínima de reserva es 5 minutos")
    private Integer reservationDurationMinutes;

    @NotNull(message = "Buffer antes de la reserva es obligatorio")
    @Min(value = 0, message = "El buffer antes no puede ser negativo")
    private Integer bufferBeforeMinutes;

    @NotNull(message = "Buffer después de la reserva es obligatorio")
    @Min(value = 0, message = "El buffer después no puede ser negativo")
    private Integer bufferAfterMinutes;
}
