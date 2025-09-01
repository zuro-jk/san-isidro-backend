package com.sanisidro.restaurante.features.restaurant.controller;

import com.sanisidro.restaurante.features.restaurant.dto.table.request.TableRequest;
import com.sanisidro.restaurante.features.restaurant.dto.table.response.TableResponse;
import com.sanisidro.restaurante.features.restaurant.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<List<TableResponse>> getAllTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableResponse> getTableById(@PathVariable Long id) {
        return ResponseEntity.ok(tableService.getTableById(id));
    }

    @PostMapping
    public ResponseEntity<TableResponse> createTable(@RequestBody @Valid TableRequest table) {
        return ResponseEntity.ok(tableService.createTable(table));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TableResponse> updateTable(@PathVariable Long id, @RequestBody @Valid TableRequest table) {
        return ResponseEntity.ok(tableService.updateTable(id, table));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
