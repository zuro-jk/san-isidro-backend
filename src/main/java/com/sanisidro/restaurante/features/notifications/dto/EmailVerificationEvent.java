package com.sanisidro.restaurante.features.notifications.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationEvent implements NotifiableEvent {
    private Long userId;
    private String recipient;
    private String subject;
    private String message;
    private String actionUrl;

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public String getRecipient() {
        return recipient;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getActionUrl() {
        return actionUrl;
    }

}
