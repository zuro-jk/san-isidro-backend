package com.sanisidro.restaurante.features.restaurant.service;

import com.sanisidro.restaurante.features.restaurant.dto.table.request.TableRequest;
import com.sanisidro.restaurante.features.restaurant.dto.table.response.TableResponse;
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
        TableEntity table = mapToEntity(request);
        return mapToResponse(tableRepository.save(table));
    }

    public TableResponse updateTable(Long id, TableRequest request) {
        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mesa no encontrada con id: " + id));

        table.setName(request.getName());
        table.setCapacity(request.getCapacity());
        table.setDescription(request.getDescription());

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
                .description(table.getDescription())
                .build();
    }

    private TableEntity mapToEntity(TableRequest request) {
        return TableEntity.builder()
                .name(request.getName())
                .capacity(request.getCapacity())
                .description(request.getDescription())
                .build();
    }

}
