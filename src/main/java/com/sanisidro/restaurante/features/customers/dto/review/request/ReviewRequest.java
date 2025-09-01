package com.sanisidro.restaurante.features.customers.dto.review.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {

    @NotNull(message = "Orden es obligatorio")
    private Long orderId;

    @NotNull(message = "Cliente es obligatorio")
    private Long customerId;

    @NotBlank(message = "Comentario es obligatorio")
    @Size(max = 500, message = "Comentario no debe exceder los 500 caracteres")
    private String comment;

    @NotNull(message = "Calificación es obligatoria")
    @Min(value = 1, message = "Calificación mínima es 1")
    @Max(value = 5, message = "Calificación máxima es 5")
    private Integer rating;

    @NotNull(message = "Fecha es obligatoria")
    private LocalDateTime date;

}
