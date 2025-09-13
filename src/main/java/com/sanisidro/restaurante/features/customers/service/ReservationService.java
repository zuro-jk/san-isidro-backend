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
import com.sanisidro.restaurante.features.restaurant.enums.TableStatus;
import com.sanisidro.restaurante.features.restaurant.model.TableEntity;
import com.sanisidro.restaurante.features.restaurant.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final TableRepository tableRepository;
    private final LoyaltyService loyaltyService;

    private static final ZoneId RESTAURANT_ZONE = ZoneId.of("America/Lima");

    private static final Map<ReservationStatus, Set<ReservationStatus>> ALLOWED_TRANSITIONS = Map.of(
            ReservationStatus.PENDING, Set.of(ReservationStatus.CONFIRMED, ReservationStatus.CANCELLED),
            ReservationStatus.CONFIRMED, Set.of(ReservationStatus.COMPLETED, ReservationStatus.CANCELLED)
    );

    /* -------------------- CREATE -------------------- */
    @Transactional
    public ReservationResponse createReservation(ReservationRequest dto) {
        Customer customer = findCustomerById(dto.getCustomerId());
        TableEntity table = findTableById(dto.getTableId());

        validateReservationFields(dto, table, null, false);

        Reservation reservation = Reservation.builder()
                .customer(customer)
                .table(table)
                .status(dto.getStatus() != null ? dto.getStatus() : ReservationStatus.PENDING)
                .build();

        reservation.updateFromDto(dto, customer, table);
        Reservation saved = reservationRepository.save(reservation);

        // Marcar mesa ocupada si es confirmada
        if (saved.getStatus() == ReservationStatus.CONFIRMED) {
            table.setStatus(TableStatus.OCCUPIED);
            tableRepository.save(table);
        }

        log.info("✅ Reserva creada: id={}, cliente={}, mesa={}, fecha={}, hora={}, status={}",
                saved.getId(), customer.getId(), table.getName(),
                saved.getReservationDate(), saved.getReservationTime(), saved.getStatus());

        return mapToResponse(saved);
    }

    @Transactional
    public ReservationResponse createWalkInReservation(Long customerId, Long tableId, int numberOfPeople) {
        Customer customer = findCustomerById(customerId);
        TableEntity table = findTableById(tableId);

        ZonedDateTime now = ZonedDateTime.now(RESTAURANT_ZONE).truncatedTo(ChronoUnit.MINUTES);

        ReservationRequest dto = new ReservationRequest();
        dto.setCustomerId(customerId);
        dto.setTableId(tableId);
        dto.setNumberOfPeople(numberOfPeople);
        dto.setReservationDate(now.toLocalDate());
        dto.setReservationTime(now.toLocalTime());
        dto.setStatus(ReservationStatus.CONFIRMED);

        validateReservationFields(dto, table, null, true);

        Reservation reservation = Reservation.builder()
                .customer(customer)
                .table(table)
                .status(ReservationStatus.CONFIRMED)
                .build();

        reservation.updateFromDto(dto, customer, table);
        Reservation saved = reservationRepository.save(reservation);

        // Marcar mesa como ocupada
        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        log.info("🚶 Walk-in manual creado: id={}, cliente={}, mesa={}, fecha={}, hora={}",
                saved.getId(),
                saved.getCustomer().getUser().getFullName(),
                saved.getTable().getName(),
                saved.getReservationDate(),
                saved.getReservationTime());

        return mapToResponse(saved);
    }

    /**
     * Walk-in con asignación automática de mesa.
     */
    @Transactional
    public ReservationResponse createAutoWalkInReservation(Long customerId, int numberOfPeople) {
        Customer customer = findCustomerById(customerId);
        ZonedDateTime now = ZonedDateTime.now(RESTAURANT_ZONE).truncatedTo(ChronoUnit.MINUTES);

        List<TableEntity> candidateTables = tableRepository.findAll()
                .stream()
                .filter(t -> t.getCapacity() >= numberOfPeople && t.getStatus() == TableStatus.FREE)
                .sorted(Comparator.comparingInt(TableEntity::getCapacity))
                .toList();

        for (TableEntity table : candidateTables) {
            log.debug("🔍 Probando mesa {} (capacidad {}) para {} personas", table.getName(), table.getCapacity(), numberOfPeople);

            ReservationRequest dto = new ReservationRequest();
            dto.setCustomerId(customerId);
            dto.setTableId(table.getId());
            dto.setNumberOfPeople(numberOfPeople);
            dto.setReservationDate(now.toLocalDate());
            dto.setReservationTime(now.toLocalTime());
            dto.setStatus(ReservationStatus.CONFIRMED);

            try {
                validateReservationFields(dto, table, null, true);

                Reservation reservation = Reservation.builder()
                        .customer(customer)
                        .table(table)
                        .status(ReservationStatus.CONFIRMED)
                        .build();

                reservation.updateFromDto(dto, customer, table);
                Reservation saved = reservationRepository.save(reservation);

                // Marcar mesa ocupada
                table.setStatus(TableStatus.OCCUPIED);
                tableRepository.save(table);

                log.info("🚀 Walk-in automático creado: id={}, cliente={}, mesa={}, fecha={}, hora={}",
                        saved.getId(),
                        saved.getCustomer().getUser().getFullName(),
                        saved.getTable().getName(),
                        saved.getReservationDate(),
                        saved.getReservationTime());

                return mapToResponse(saved);

            } catch (InvalidReservationException e) {
                log.debug("❌ Mesa {} descartada: {}", table.getName(), e.getMessage());
            }
        }

        throw new InvalidReservationException("No hay mesas disponibles para " + numberOfPeople + " personas en este momento");
    }

    /* -------------------- READ -------------------- */
    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Long id) {
        return mapToResponse(findReservationById(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReservationResponse> getReservationsByCustomer(Long customerId, Pageable pageable) {
        Page<Reservation> page = reservationRepository.findByCustomerId(customerId, pageable);
        List<ReservationResponse> content = page.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();
        return buildPagedResponse(page, content);
    }

    /* -------------------- UPDATE -------------------- */
    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationRequest dto) {
        Reservation reservation = findReservationById(id);
        Customer customer = findCustomerById(dto.getCustomerId());
        TableEntity table = findTableById(dto.getTableId());

        validateReservationFields(dto, table, id, false);
        reservation.updateFromDto(dto, customer, table);

        Reservation updated = reservationRepository.save(reservation);

        log.info("✏️ Reserva actualizada: id={}, cliente={}, mesa={}, fecha={}, hora={}",
                updated.getId(), customer.getId(), table.getName(),
                updated.getReservationDate(), updated.getReservationTime());

        return mapToResponse(updated);
    }

    /* -------------------- COMPLETE -------------------- */
    @Transactional
    public ReservationResponse completeReservation(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        validateStateTransition(reservation, ReservationStatus.COMPLETED);

        reservation.setStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);

        TableEntity table = reservation.getTable();
        table.setStatus(TableStatus.FREE);
        tableRepository.save(table);

        int points = loyaltyService.calculatePoints(
                reservation.getCustomer(),
                null,
                "RESERVATION_COMPLETE",
                reservation.getNumberOfPeople()
        );

        Customer customer = reservation.getCustomer();
        customer.setPoints(customer.getPoints() + points);
        customerRepository.save(customer);

        log.info("🏁 Reserva completada: id={}, cliente={}, mesa={}, fecha={}, hora={}, puntos={}",
                reservation.getId(), customer.getId(), table.getName(),
                reservation.getReservationDate(), reservation.getReservationTime(), points);

        return mapToResponse(reservation);
    }

    @Transactional
    public ReservationResponse confirmReservation(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        validateStateTransition(reservation, ReservationStatus.CONFIRMED);

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        TableEntity table = reservation.getTable();
        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        log.info("✅ Reserva confirmada: id={}, cliente={}, mesa={}, fecha={}, hora={}, status={}",
                reservation.getId(),
                reservation.getCustomer().getId(),
                table.getName(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getStatus());

        return mapToResponse(reservation);
    }

    /* -------------------- CANCEL -------------------- */
    @Transactional
    public ReservationResponse cancelReservation(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        validateStateTransition(reservation, ReservationStatus.CANCELLED);

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // Liberar mesa
        TableEntity table = reservation.getTable();
        table.setStatus(TableStatus.FREE);
        tableRepository.save(table);

        log.info("❌ Reserva cancelada: id={}, cliente={}, mesa={}, fecha={}, hora={}",
                reservation.getId(), reservation.getCustomer().getId(), table.getName(),
                reservation.getReservationDate(), reservation.getReservationTime());

        return mapToResponse(reservation);
    }

    /* -------------------- DELETE -------------------- */
    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = findReservationById(id);
        reservationRepository.delete(reservation);
        log.info("🗑️ Reserva eliminada: id={}, cliente={}", id, reservation.getCustomer().getId());
    }

    /* -------------------- VALIDATIONS -------------------- */
    private void validateReservationFields(ReservationRequest dto, TableEntity table, Long currentReservationId, boolean isWalkIn) {
        if (dto.getNumberOfPeople() <= 0)
            throw new InvalidReservationException("El número de personas debe ser mayor a 0");

        if (dto.getNumberOfPeople() > table.getCapacity())
            throw new InvalidReservationException("La mesa no soporta esa cantidad de personas");

        ZonedDateTime now = ZonedDateTime.now(RESTAURANT_ZONE);
        ZonedDateTime reservationDateTime = ZonedDateTime.of(dto.getReservationDate(), dto.getReservationTime(), RESTAURANT_ZONE);

        if (isWalkIn) {
            if (reservationDateTime.isBefore(now.minusMinutes(5))) {
                throw new InvalidReservationException("La reserva no puede estar en el pasado (más de 5 minutos)");
            }
        } else {
            ZonedDateTime minAllowed = now.plusMinutes(15);
            if (reservationDateTime.isBefore(minAllowed)) {
                throw new InvalidReservationException("Las reservas deben hacerse con al menos 15 minutos de anticipación");
            }
        }

        int bufferBefore = table.getBufferBeforeMinutes();
        int bufferAfter = table.getBufferAfterMinutes();

        LocalTime reservationStart = dto.getReservationTime().minusMinutes(bufferBefore);
        LocalTime reservationEnd = dto.getReservationTime()
                .plusMinutes(table.getReservationDurationMinutes() + bufferAfter);

        if (reservationStart.isBefore(table.getOpenTime()) || reservationEnd.isAfter(table.getCloseTime())) {
            throw new InvalidReservationException("La reserva debe estar dentro del horario de la mesa: "
                    + table.getOpenTime() + " - " + table.getCloseTime());
        }

        List<Reservation> reservations = reservationRepository
                .findByTable_IdAndReservationDate(table.getId(), dto.getReservationDate());

        for (Reservation r : reservations) {
            if (currentReservationId != null && r.getId().equals(currentReservationId)) continue;

            int existingBufferBefore = r.getTable().getBufferBeforeMinutes();
            int existingBufferAfter = r.getTable().getBufferAfterMinutes();

            LocalTime existingStart = r.getReservationTime().minusMinutes(existingBufferBefore);
            LocalTime existingEnd = r.getReservationTime()
                    .plusMinutes(r.getTable().getReservationDurationMinutes() + existingBufferAfter);

            boolean overlaps = reservationStart.isBefore(existingEnd) && reservationEnd.isAfter(existingStart);
            if (overlaps) {
                throw new InvalidReservationException(
                        "La mesa ya está ocupada en ese intervalo de tiempo (incluyendo tiempo de limpieza y preparación)"
                );
            }
        }
    }

    private void validateStateTransition(Reservation reservation, ReservationStatus newStatus) {
        if (ALLOWED_TRANSITIONS.getOrDefault(reservation.getStatus(), Set.of()).contains(newStatus)) return;
        throw new InvalidReservationException("No se puede cambiar de " + reservation.getStatus() + " a " + newStatus);
    }

    /* -------------------- HELPERS -------------------- */
    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    private TableEntity findTableById(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada"));
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .customerId(reservation.getCustomer() != null ? reservation.getCustomer().getId() : null)
                .customerName(reservation.getCustomer() != null ? reservation.getCustomer().getUser().getFullName() : null)
                .tableId(reservation.getTable() != null ? reservation.getTable().getId() : null)
                .tableNumber(reservation.getTable() != null ? reservation.getTable().getName() : null)
                .contactName(reservation.getContactName())
                .contactPhone(reservation.getContactPhone())
                .reservationDate(reservation.getReservationDate())
                .reservationTime(reservation.getReservationTime())
                .numberOfPeople(reservation.getNumberOfPeople())
                .status(reservation.getStatus())
                .build();
    }

    private <T> PagedResponse<T> buildPagedResponse(Page<?> page, List<T> content) {
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

}
