package com.sanisidro.restaurante.features.suppliers.dto.supplier.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequest {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    private String companyName;

    @NotBlank(message = "El nombre del contacto es obligatorio")
    private String contactName;

    private String phone;

    private String address;
}
