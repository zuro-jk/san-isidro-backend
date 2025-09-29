package com.sanisidro.restaurante.features.customers.dto.address.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressAdminRequest {

    @NotNull(message = "El cliente es obligatorio")
    private Long customerId;

    @NotBlank(message = "La calle y número son obligatorios")
    @Size(min = 5, max = 255, message = "La calle debe tener entre 5 y 255 caracteres")
    private String street;

    @Size(max = 255, message = "La referencia no debe exceder los 255 caracteres")
    private String reference;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100, message = "La ciudad no debe exceder los 100 caracteres")
    private String city;

    @NotBlank(message = "La provincia es obligatoria")
    @Size(max = 100, message = "La provincia no debe exceder los 100 caracteres")
    private String province;

    @Size(max = 20, message = "El código postal no debe exceder los 20 caracteres")
    private String zipCode;

    @Size(max = 255, message = "Las instrucciones no deben exceder los 255 caracteres")
    private String instructions;
}
