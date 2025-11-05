package com.sanisidro.restaurante.features.customers.dto.customer.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {
    @NotNull
    private Long userId;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private Integer points;
}
