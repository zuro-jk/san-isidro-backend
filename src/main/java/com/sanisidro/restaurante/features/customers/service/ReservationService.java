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
import com.sanisidro.restaurante.features.restaurant.model.TableEntity;
import com.sanisidro.restaurante.features.restaurant.repository.TableRepository;
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
    private final TableRepository tableRepository;

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
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        TableEntity table = tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada"));

        validateReservationFields(dto, table, null);

        ReservationStatus status = dto.getStatus() != null ? dto.getStatus() : ReservationStatus.PENDING;

        Reservation reservation = Reservation.builder()
                .customer(customer)
                .table(table)
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

        TableEntity table = tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada"));

        validateReservationFields(dto, table, id);

        // Asignamos los campos
        reservation.setCustomer(customer);
        reservation.setTable(table);
        reservation.setContactName(dto.getContactName());
        reservation.setContactPhone(dto.getContactPhone());
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setReservationTime(dto.getReservationTime());
        reservation.setNumberOfPeople(dto.getNumberOfPeople());
        // Mantiene el status si no viene en el DTO
        reservation.setStatus(dto.getStatus() != null ? dto.getStatus() : reservation.getStatus());

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
                .tableId(reservation.getTable() != null ? reservation.getTable().getId() : null)
                .contactName(reservation.getContactName())
                .contactPhone(reservation.getContactPhone())
                .reservationDate(reservation.getReservationDate())
                .reservationTime(reservation.getReservationTime())
                .numberOfPeople(reservation.getNumberOfPeople())
                .status(reservation.getStatus())
                .build();
    }

    private void validateReservationFields(ReservationRequest dto, TableEntity table, Long currentReservationId) {
        if (dto.getNumberOfPeople() <= 0) {
            throw new InvalidReservationException("El número de personas debe ser mayor a 0");
        }

        if (dto.getNumberOfPeople() > table.getCapacity()) {
            throw new InvalidReservationException("La mesa no soporta esa cantidad de personas");
        }

        if (dto.getReservationDate().isBefore(LocalDate.now()) ||
                (dto.getReservationDate().isEqual(LocalDate.now()) &&
                        dto.getReservationTime().isBefore(LocalTime.now()))) {
            throw new InvalidReservationException("La fecha y hora de la reserva no puede estar en el pasado");
        }

        // Validación duplicado por cliente
        if (reservationRepository.existsByCustomer_IdAndReservationDateAndReservationTime(
                dto.getCustomerId(), dto.getReservationDate(), dto.getReservationTime())) {
            throw new InvalidReservationException("El cliente ya tiene una reserva para esta fecha y hora");
        }

        // Validación mesa ocupada, excluyendo la propia reserva en update
        boolean mesaOcupada = reservationRepository.existsByTable_IdAndReservationDateAndReservationTime(
                dto.getTableId(), dto.getReservationDate(), dto.getReservationTime());

        if (mesaOcupada && (currentReservationId == null ||
                !reservationRepository.findById(currentReservationId)
                        .map(r -> r.getTable().getId().equals(dto.getTableId())
                                && r.getReservationDate().equals(dto.getReservationDate())
                                && r.getReservationTime().equals(dto.getReservationTime()))
                        .orElse(false))) {
            throw new InvalidReservationException("La mesa ya está reservada en esa fecha y hora");
        }
    }
}
