package com.sanisidro.restaurante.features.suppliers.dto.supplier.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierResponse {
    private Long id;
    private String companyName;
    private String contactName;
    private String phone;
    private String address;
    private String username;
    private String email;
    private Long userId;
}
