package com.sanisidro.restaurante.features.customers.dto.review.response;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;

    private Long customerId;

    private String comment;

    private Integer rating;

    private LocalDateTime date;
}
