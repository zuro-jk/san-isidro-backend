package com.sanisidro.restaurante.core.email.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMessageRequest {

    @Email(message = "Debe ser un email valido")
    @NotBlank(message = "El destinatario no puede estar vacio")
    private String toAddress;

    @NotBlank(message = "El asunto no puede estar vacio")
    private String subject;

    @NotBlank(message = "El cuerpo no puede estar vacio")
    private String body;
}
