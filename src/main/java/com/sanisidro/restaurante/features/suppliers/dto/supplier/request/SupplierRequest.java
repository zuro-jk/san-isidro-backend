package com.sanisidro.restaurante.features.suppliers.dto.supplier.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequest {

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    private String name;

    private String contact;

    private String address;
}
