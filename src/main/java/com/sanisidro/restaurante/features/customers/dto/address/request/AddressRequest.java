package com.sanisidro.restaurante.features.customers.dto.address.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {

    private Long customerId;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
    private String address;

    @Size(max = 255, message = "La referencia no debe exceder los 255 caracteres")
    private String reference;
}
