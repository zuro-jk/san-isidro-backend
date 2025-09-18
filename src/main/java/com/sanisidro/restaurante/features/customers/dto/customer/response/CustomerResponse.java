package com.sanisidro.restaurante.features.customers.dto.customer.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private Integer points;
}
