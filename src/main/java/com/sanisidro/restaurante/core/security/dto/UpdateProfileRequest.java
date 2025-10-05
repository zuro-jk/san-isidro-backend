package com.sanisidro.restaurante.core.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateProfileRequest {
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 50, message = "El apellido no puede tener más de 50 caracteres")
    private String lastName;

    @Email(message = "El correo no es válido")
    @NotBlank(message = "El correo no puede estar vacío")
    @Size(max = 100, message = "El correo no puede tener más de 100 caracteres")
    private String email;

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(max = 30, message = "El nombre de usuario no puede tener más de 30 caracteres")
    private String username;

    private String phone;
}
