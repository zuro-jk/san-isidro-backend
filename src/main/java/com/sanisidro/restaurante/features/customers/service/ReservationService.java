package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.exceptions.InvalidReservationException;
import com.sanisidro.restaurante.core.exceptions.ResourceNotFoundException;
import com.sanisidro.restaurante.features.customers.dto.reservation.request.ReservationRequest;
import com.sanisidro.restaurante.features.customers.dto.reservation.response.ReservationResponse;
import com.sanisidro.restaurante.features.customers.enums.ReservationStatus;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.model.Reservation;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.customers.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;

    public ReservationResponse getReservation(Long id) {
        return mapToResponse(reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation no encontrada")));
    }

    public PagedResponse<ReservationResponse> getReservationsByCustomer(Long customerId, Pageable pageable) {
        Page<Reservation> page = reservationRepository.findByCustomerId(customerId, pageable);
        List<ReservationResponse> content = page.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public ReservationResponse createReservation(ReservationRequest dto) {
        // Validación de duplicado
        if (reservationRepository.existsByCustomer_IdAndReservationDateAndReservationTime(
                dto.getCustomerId(), dto.getReservationDate(), dto.getReservationTime())) {
            throw new InvalidReservationException("El cliente ya tiene una reserva para esta fecha y hora");
        }

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        validateReservationFields(dto);

        // Convertimos status String a Enum de forma segura
        ReservationStatus status = dto.getStatus() != null ? dto.getStatus() : ReservationStatus.PENDING;

        Reservation reservation = Reservation.builder()
                .customer(customer)
                .contactName(dto.getContactName())
                .contactPhone(dto.getContactPhone())
                .reservationDate(dto.getReservationDate())
                .reservationTime(dto.getReservationTime())
                .numberOfPeople(dto.getNumberOfPeople())
                .status(status)
                .build();

        return mapToResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse updateReservation(Long id, ReservationRequest dto) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation no encontrada"));

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        validateReservationFields(dto);

        // Asignamos los campos
        reservation.setCustomer(customer);
        reservation.setContactName(dto.getContactName());
        reservation.setContactPhone(dto.getContactPhone());
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setReservationTime(dto.getReservationTime());
        reservation.setNumberOfPeople(dto.getNumberOfPeople());

        // Validación de enum aplicada desde DTO, si es null usamos PENDING
        reservation.setStatus(dto.getStatus() != null ? dto.getStatus() : ReservationStatus.PENDING);

        return mapToResponse(reservationRepository.save(reservation));
    }

    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation no encontrada");
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

    private void validateReservationFields(ReservationRequest dto) {
        if (dto.getNumberOfPeople() <= 0) {
            throw new InvalidReservationException("El número de personas debe ser mayor a 0");
        }

        if (dto.getReservationDate().isBefore(LocalDate.now()) ||
                (dto.getReservationDate().isEqual(LocalDate.now()) &&
                        dto.getReservationTime().isBefore(LocalTime.now()))) {
            throw new InvalidReservationException("La fecha y hora de la reserva no puede estar en el pasado");
        }
    }
}
