package com.sanisidro.restaurante.features.customers.repository;

import com.sanisidro.restaurante.features.customers.enums.ReservationStatus;
import com.sanisidro.restaurante.features.customers.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCustomerId(Long customerId);
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByReservationDate(LocalDate date);

    boolean existsByCustomer_IdAndReservationDateAndReservationTime(
            Long customerId, LocalDate reservationDate, LocalTime reservationTime
    );
}
