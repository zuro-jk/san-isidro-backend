package com.sanisidro.restaurante.features.customers.dto.review.response;

import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;

    private Long customerId;
    private String customerName;

    private Long orderId;
    private Long reservationId;
    private Long productId;

    private String comment;
    private Integer rating;

    private LocalDateTime date;
}