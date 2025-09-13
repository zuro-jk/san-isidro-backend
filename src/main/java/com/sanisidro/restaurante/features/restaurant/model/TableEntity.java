package com.sanisidro.restaurante.features.restaurant.model;

import com.sanisidro.restaurante.features.restaurant.enums.TableStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "description")
    private String description;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "reservation_duration_minutes", nullable = false)
    private Integer reservationDurationMinutes;

    @Column(name = "buffer_before_minutes", nullable = false)
    private Integer bufferBeforeMinutes;

    @Column(name = "buffer_after_minutes", nullable = false)
    private Integer bufferAfterMinutes;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TableStatus status;
}
