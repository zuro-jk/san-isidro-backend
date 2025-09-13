package com.sanisidro.restaurante.features.customers.repository;

import com.sanisidro.restaurante.features.customers.enums.ReservationStatus;
import com.sanisidro.restaurante.features.customers.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByCustomerId(Long customerId, Pageable pageable);
    List<Reservation> findByTable_IdAndReservationDate(Long tableId, LocalDate date);

    List<Reservation> findByStatusAndReservationDateOrderByReservationTimeAsc(
            ReservationStatus status,
            LocalDate reservationDate
    );
}
