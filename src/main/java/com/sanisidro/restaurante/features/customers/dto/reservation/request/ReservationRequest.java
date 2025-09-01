package com.sanisidro.restaurante.features.customers.dto.reservation.request;

import com.sanisidro.restaurante.features.customers.enums.ReservationStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequest {

    @NotNull(message = "Cliente es obligatorio")
    private Long customerId;

    @NotBlank(message = "Nombre de contacto es obligatorio")
    @Size(max = 100, message = "Nombre de contacto no debe exceder los 100 caracteres")
    private String contactName;

    @NotBlank(message = "Teléfono de contacto es obligatorio")
    @Size(min = 7, max = 15, message = "Teléfono de contacto debe tener entre 7 y 15 caracteres")
    private String contactPhone;

    @NotNull(message = "Fecha de reserva es obligatoria")
    @FutureOrPresent(message = "La fecha de reserva no puede ser en el pasado")
    private LocalDate reservationDate;

    @NotNull(message = "Hora de reserva es obligatoria")
    private LocalTime reservationTime;

    @NotNull(message = "Número de personas es obligatorio")
    @Min(value = 1, message = "Minimo 1 persona por reserva")
    @Max(value = 50, message = "Numero máximo de personas por reserva es 50")
    private Integer numberOfPeople;

    private ReservationStatus status;
}
