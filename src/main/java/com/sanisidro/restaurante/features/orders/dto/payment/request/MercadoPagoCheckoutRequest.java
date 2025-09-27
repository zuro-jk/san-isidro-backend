package com.sanisidro.restaurante.features.orders.dto.payment.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MercadoPagoCheckoutRequest {

    @NotNull(message = "El ID de la orden es obligatorio")
    private Long orderId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ser un correo válido")
    private String email;

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String docType;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento no puede superar los 20 caracteres")
    private String docNumber;

    @NotBlank(message = "El teléfono es obligatorio")
    private String phone;

    private String areaCode = "51";

    @NotBlank(message = "La calle es obligatoria")
    private String street;

    @NotBlank(message = "La ciudad es obligatoria")
    private String city;

    private String zipCode = "15001";

    private String paymentMethodId = "visa";
}