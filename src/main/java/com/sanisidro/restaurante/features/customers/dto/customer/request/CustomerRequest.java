package com.sanisidro.restaurante.features.customers.dto.customer.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {

    @NotNull
    private Long userId;

    private Integer points;
}
