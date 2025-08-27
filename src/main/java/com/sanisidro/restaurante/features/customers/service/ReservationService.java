package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.features.customers.dto.reservation.request.ReservationRequest;
import com.sanisidro.restaurante.features.customers.dto.reservation.response.ReservationResponse;
import com.sanisidro.restaurante.features.customers.enums.ReservationStatus;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.model.Reservation;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.customers.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;

    public ReservationResponse createReservation(ReservationRequest dto) {
        Customer customer = null;
        if (dto.getCustomerId() != null) {
            customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
        }

        Reservation reservation = Reservation.builder()
                .customer(customer)
                .contactName(dto.getContactName())
                .contactPhone(dto.getContactPhone())
                .reservationDate(dto.getReservationDate())
                .reservationTime(dto.getReservationTime())
                .numberOfPeople(dto.getNumberOfPeople())
                .status(dto.getStatus() != null ? dto.getStatus() : ReservationStatus.PENDING)
                .build();

        return mapToResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse getReservation(Long id) {
        return mapToResponse(reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found")));
    }

    public List<ReservationResponse> getReservationsByCustomer(Long customerId) {
        return reservationRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReservationResponse updateReservation(Long id, ReservationRequest dto) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (dto.getReservationDate() != null) reservation.setReservationDate(dto.getReservationDate());
        if (dto.getReservationTime() != null) reservation.setReservationTime(dto.getReservationTime());
        if (dto.getNumberOfPeople() != null) reservation.setNumberOfPeople(dto.getNumberOfPeople());
        if (dto.getStatus() != null) reservation.setStatus(dto.getStatus());
        if (dto.getContactName() != null) reservation.setContactName(dto.getContactName());
        if (dto.getContactPhone() != null) reservation.setContactPhone(dto.getContactPhone());

        return mapToResponse(reservationRepository.save(reservation));
    }

    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new RuntimeException("Reservation not found");
        }
        reservationRepository.deleteById(id);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .customerId(reservation.getCustomer() != null ? reservation.getCustomer().getId() : null)
                .contactName(reservation.getContactName())
                .contactPhone(reservation.getContactPhone())
                .reservationDate(reservation.getReservationDate())
                .reservationTime(reservation.getReservationTime())
                .numberOfPeople(reservation.getNumberOfPeople())
                .status(reservation.getStatus())
                .build();
    }
}
