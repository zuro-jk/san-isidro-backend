package com.sanisidro.restaurante.features.suppliers.dto.supplier.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierRequest {

    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")
    private String username;

    @Email(message = "El formato del email no es válido")
    private String email;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    private String companyName;

    @NotBlank(message = "El nombre del contacto es obligatorio")
    private String contactName;

    private String phone;

    private String address;
}
