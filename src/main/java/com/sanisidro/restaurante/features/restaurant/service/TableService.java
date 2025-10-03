package com.sanisidro.restaurante.features.restaurant.service;

import com.sanisidro.restaurante.core.config.ReservationProperties;
import com.sanisidro.restaurante.features.customers.model.Reservation;
import com.sanisidro.restaurante.features.customers.repository.ReservationRepository;
import com.sanisidro.restaurante.features.restaurant.dto.table.request.TableRequest;
import com.sanisidro.restaurante.features.restaurant.dto.table.response.TableAvailabilityResponse;
import com.sanisidro.restaurante.features.restaurant.dto.table.response.TableResponse;
import com.sanisidro.restaurante.features.restaurant.enums.TableStatus;
import com.sanisidro.restaurante.features.restaurant.model.TableEntity;
import com.sanisidro.restaurante.features.restaurant.repository.TableRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableService {

    private final TableRepository tableRepository;
    private final ReservationProperties reservationProperties;
    private final ReservationRepository reservationRepository;


    public List<TableResponse> getAllTables() {
        return tableRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TableResponse getTableById(Long id) {
        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mesa no encontrada con id: " + id));
        return mapToResponse(table);
    }

    public TableResponse createTable(TableRequest request) {
        validateTableRequest(request);
        TableEntity table = mapToEntity(request);
        table.setStatus(TableStatus.FREE);
        return mapToResponse(tableRepository.save(table));
    }

    public TableResponse updateTable(Long id, TableRequest request) {
        validateTableRequest(request);
        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mesa no encontrada con id: " + id));

        table.setCode(request.getCode());
        table.setAlias(request.getAlias());
        table.setCapacity(request.getCapacity());
        table.setMinCapacity(request.getMinCapacity());
        table.setOptimalCapacity(request.getOptimalCapacity());
        table.setPriority(request.getPriority());
        table.setDescription(request.getDescription());
        table.setOpenTime(request.getOpenTime());
        table.setCloseTime(request.getCloseTime());
        table.setReservationDurationMinutes(request.getReservationDurationMinutes());
        table.setBufferBeforeMinutes(request.getBufferBeforeMinutes());
        table.setBufferAfterMinutes(request.getBufferAfterMinutes());
        table.setStatus(request.getStatus());

        return mapToResponse(tableRepository.save(table));
    }

    public void deleteTable(Long id) {
        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mesa no encontrada con id: " + id));
        tableRepository.delete(table);
    }

    /* -------------------- DISPONIBILIDAD -------------------- */

    public boolean isTableAvailable(Long tableId, String startTime, int numberOfPeople, boolean includeBuffers) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new EntityNotFoundException("Mesa no encontrada con id: " + tableId));

        if (!table.canAccommodate(numberOfPeople)) return false;

        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = start.plusMinutes(table.getReservationDurationMinutes());

        if (includeBuffers) {
            int before = table.getBufferBeforeMinutes() != null ? table.getBufferBeforeMinutes() : reservationProperties.getBufferBeforeMinutes();
            int after = table.getBufferAfterMinutes() != null ? table.getBufferAfterMinutes() : reservationProperties.getBufferAfterMinutes();

            start = start.minusMinutes(before);
            end = end.plusMinutes(after);
        }

        // Verificar si la mesa está abierta en ese horario
        LocalTime reservationStartTime = start.toLocalTime();
        LocalTime reservationEndTime = end.toLocalTime();
        if (reservationStartTime.isBefore(table.getOpenTime()) || reservationEndTime.isAfter(table.getCloseTime())) {
            return false;
        }

        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(tableId, start, end);
        return overlapping.isEmpty();
    }

    /* -------------------- MESAS DISPONIBLES Y ÓPTIMA -------------------- */

    public List<TableResponse> getAvailableTables(int numberOfPeople, String startTime) {
        return tableRepository.findAll()
                .stream()
                .filter(t -> t.canAccommodate(numberOfPeople))
                .filter(t -> isTableAvailable(t.getId(), startTime, numberOfPeople, true))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TableResponse findOptimalTable(int numberOfPeople, String startTime) {
        return tableRepository.findAll()
                .stream()
                .filter(t -> t.canAccommodate(numberOfPeople))
                .filter(t -> isTableAvailable(t.getId(), startTime, numberOfPeople, true))
                .min(Comparator.comparingInt(t -> t.getOptimalCapacity()))
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("No hay mesas disponibles para este horario y número de personas"));
    }

    public List<TableAvailabilityResponse> getTablesWithAvailableTimes(int numberOfPeople, LocalDateTime date, boolean filterByCapacity) {
        List<TableAvailabilityResponse> results = new ArrayList<>();

        for (TableEntity table : tableRepository.findAll()) {

            if (filterByCapacity && !table.canAccommodate(numberOfPeople)) {
                log.info("Mesa {} descartada: no puede acomodar {} personas.", table.getCode(), numberOfPeople);
                continue;
            }

            List<String> availableTimes = calculateAvailableTimes(table, date);
            log.info("Mesa {} - Horarios calculados: {}", table.getCode(), availableTimes);

            TableAvailabilityResponse response = TableAvailabilityResponse.builder()
                    .id(table.getId())
                    .name(table.getCode())
                    .capacity(table.getCapacity())
                    .minCapacity(table.getMinCapacity())
                    .availableTimes(availableTimes)
                    .build();

            results.add(response);
        }

        log.info("TOTAL mesas retornadas: {}", results.size());
        return results;
    }

    private List<String> calculateAvailableTimes(TableEntity table, LocalDateTime date) {
        List<String> times = new ArrayList<>();
        LocalDateTime start = LocalDateTime.of(date.toLocalDate(), table.getOpenTime());
        LocalDateTime endOfDay = LocalDateTime.of(date.toLocalDate(), table.getCloseTime());
        int incrementMinutes = 30;

        int bufferBefore = table.getBufferBeforeMinutes() != null ? table.getBufferBeforeMinutes() : reservationProperties.getBufferBeforeMinutes();
        int bufferAfter = table.getBufferAfterMinutes() != null ? table.getBufferAfterMinutes() : reservationProperties.getBufferAfterMinutes();

        while (!start.plusMinutes(table.getReservationDurationMinutes()).isAfter(endOfDay)) {
            LocalDateTime slotStart = start.minusMinutes(bufferBefore);
            LocalDateTime slotEnd = start.plusMinutes(table.getReservationDurationMinutes() + bufferAfter);

            if (slotStart.toLocalTime().isBefore(table.getOpenTime()) || slotEnd.toLocalTime().isAfter(table.getCloseTime())) {
                start = start.plusMinutes(incrementMinutes);
                continue;
            }

            List<Reservation> overlapping = reservationRepository.findOverlappingReservations(table.getId(), slotStart, slotEnd);

            if (overlapping.isEmpty()) {
                times.add(start.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }

            start = start.plusMinutes(incrementMinutes);
        }

        return times;
    }

    /* -------------------- UTILIDADES -------------------- */

    private TableResponse mapToResponse(TableEntity table) {
        return TableResponse.builder()
                .id(table.getId())
                .code(table.getCode())
                .alias(table.getAlias())
                .capacity(table.getCapacity())
                .minCapacity(table.getMinCapacity())
                .optimalCapacity(table.getOptimalCapacity())
                .priority(table.getPriority())
                .description(table.getDescription())
                .openTime(table.getOpenTime())
                .closeTime(table.getCloseTime())
                .reservationDurationMinutes(table.getReservationDurationMinutes())
                .bufferBeforeMinutes(table.getBufferBeforeMinutes())
                .bufferAfterMinutes(table.getBufferAfterMinutes())
                .status(table.getStatus())
                .build();
    }

    private TableEntity mapToEntity(TableRequest request) {
        return TableEntity.builder()
                .code(request.getCode())
                .alias(request.getAlias())
                .capacity(request.getCapacity())
                .minCapacity(request.getMinCapacity())
                .optimalCapacity(request.getOptimalCapacity())
                .priority(request.getPriority())
                .description(request.getDescription())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .reservationDurationMinutes(request.getReservationDurationMinutes())
                .bufferBeforeMinutes(request.getBufferBeforeMinutes())
                .bufferAfterMinutes(request.getBufferAfterMinutes())
                .status(request.getStatus())
                .build();
    }

    private void validateTableRequest(TableRequest request) {
        if (request.getMinCapacity() > request.getOptimalCapacity()) {
            throw new IllegalArgumentException("La capacidad mínima no puede ser mayor que la capacidad óptima");
        }
        if (request.getOptimalCapacity() > request.getCapacity()) {
            throw new IllegalArgumentException("La capacidad óptima no puede ser mayor que la capacidad máxima");
        }
        if (request.getOpenTime().isAfter(request.getCloseTime()) || request.getOpenTime().equals(request.getCloseTime())) {
            throw new IllegalArgumentException("La hora de apertura debe ser anterior a la hora de cierre");
        }
        if (request.getReservationDurationMinutes() <= 0) {
            throw new IllegalArgumentException("La duración de la reserva debe ser mayor a 0");
        }
        if (request.getBufferBeforeMinutes() < 0 || request.getBufferAfterMinutes() < 0) {
            throw new IllegalArgumentException("Los buffers no pueden ser negativos");
        }
    }

}
