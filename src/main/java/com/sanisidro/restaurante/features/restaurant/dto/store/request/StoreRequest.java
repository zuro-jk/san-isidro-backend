package com.sanisidro.restaurante.features.restaurant.dto.store.request;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoreRequest {

    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String name;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
    private String address;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    private String phone;

    @NotNull(message = "La hora de apertura es obligatoria")
    private LocalTime openTime;

    @NotNull(message = "La hora de cierre es obligatoria")
    private LocalTime closeTime;
}