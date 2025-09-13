package com.sanisidro.restaurante.features.customers.dto.address.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private Long id;
    private Long customerId;
    private String address;
    private String reference;
    private String createdAt;
    private String updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
