package com.sanisidro.restaurante.features.notifications.model;

import com.sanisidro.restaurante.core.security.model.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "email_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmailNotification extends Notification{

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", columnDefinition = "text", nullable = false)
    private String body;

    @Column(name = "to_address", nullable = false)
    private String toAddress;

}
