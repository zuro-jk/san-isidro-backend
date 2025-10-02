package com.sanisidro.restaurante.features.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactNotificationEvent implements NotifiableEvent {

    private Long userId;

    @NotBlank(message = "El asunto es obligatorio")
    private String subject;

    @NotBlank(message = "El mensaje no puede estar vacío")
    private String message;

    private String actionUrl;

    private String name;
    private String email;
    private String phone;

    @Override
    public Long getUserId() { return userId; }

    @Override
    public String getRecipient() {
        return "DarckProyect2@gmail.com";
    }

    @Override
    public String getSubject() { return subject; }

    @Override
    public String getMessage() { return message; }

    @Override
    public String getActionUrl() { return actionUrl; }

    @Override
    public String getChannelKey() {
        return "EMAIL";
    }
}