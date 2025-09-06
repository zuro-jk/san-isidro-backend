package com.sanisidro.restaurante.features.notifications.model;

import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.features.notifications.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@SuperBuilder
public abstract class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "status", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

}
