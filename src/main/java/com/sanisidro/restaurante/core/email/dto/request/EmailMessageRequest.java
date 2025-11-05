package com.sanisidro.restaurante.core.email.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String pdfAttachmentBase64;
    private String attachmentName;
}
