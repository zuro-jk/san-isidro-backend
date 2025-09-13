package com.sanisidro.restaurante.features.customers.dto.review.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewRequest {
    private Long id;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long customerId;

    private Long orderId;
    private Long reservationId;
    private Long productId;

    private String comment;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer rating;

}
