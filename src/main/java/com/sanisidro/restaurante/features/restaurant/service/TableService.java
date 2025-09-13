package com.sanisidro.restaurante.features.restaurant.service;

import com.sanisidro.restaurante.features.restaurant.dto.table.request.TableRequest;
import com.sanisidro.restaurante.features.restaurant.dto.table.response.TableResponse;
import com.sanisidro.restaurante.features.restaurant.enums.TableStatus;
import com.sanisidro.restaurante.features.restaurant.model.TableEntity;
import com.sanisidro.restaurante.features.restaurant.repository.TableRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;

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
        validateTableRequest(request); // <- nueva validación cruzada
        TableEntity table = mapToEntity(request);
        table.setStatus(TableStatus.FREE);
        return mapToResponse(tableRepository.save(table));
    }

    public TableResponse updateTable(Long id, TableRequest request) {
        validateTableRequest(request); // <- validación cruzada
        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mesa no encontrada con id: " + id));

        table.setName(request.getName());
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

        return mapToResponse(tableRepository.save(table));
    }

    public void deleteTable(Long id) {
        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mesa no encontrada con id: " + id));
        tableRepository.delete(table);
    }

    private TableResponse mapToResponse(TableEntity table) {
        return TableResponse.builder()
                .id(table.getId())
                .name(table.getName())
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
                .name(request.getName())
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
