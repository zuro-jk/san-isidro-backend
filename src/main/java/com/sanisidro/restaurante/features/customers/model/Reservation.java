package com.sanisidro.restaurante.features.customers.model;

import com.sanisidro.restaurante.core.model.Auditable;
import com.sanisidro.restaurante.features.customers.dto.reservation.request.ReservationRequest;
import com.sanisidro.restaurante.features.customers.enums.ReservationStatus;
import com.sanisidro.restaurante.features.restaurant.model.TableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "reservation_id"))
public class Reservation extends Auditable {

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "reservation_time", nullable = false)
    private LocalTime reservationTime;

    @Column(name = "number_of_people", nullable = false)
    private Integer numberOfPeople;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    private TableEntity table;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private ReservationStatus status = ReservationStatus.PENDING;

    public void updateFromDto(ReservationRequest dto, Customer customer, TableEntity table) {
        if (dto.getContactName() != null) this.contactName = dto.getContactName().trim();
        if (dto.getContactPhone() != null) this.contactPhone = dto.getContactPhone().trim();
        if (dto.getReservationDate() != null) this.reservationDate = dto.getReservationDate();
        if (dto.getReservationTime() != null) this.reservationTime = dto.getReservationTime();
        if (dto.getNumberOfPeople() != null) this.numberOfPeople = dto.getNumberOfPeople();
        if (dto.getStatus() != null) this.status = dto.getStatus();
        if (customer != null) this.customer = customer;
        if (table != null) this.table = table;
    }
}
