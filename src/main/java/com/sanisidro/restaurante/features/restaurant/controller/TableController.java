package com.sanisidro.restaurante.features.restaurant.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.restaurant.dto.table.request.TableRequest;
import com.sanisidro.restaurante.features.restaurant.dto.table.response.TableAvailabilityResponse;
import com.sanisidro.restaurante.features.restaurant.dto.table.response.TableResponse;
import com.sanisidro.restaurante.features.restaurant.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping("/available-times")
    public ResponseEntity<ApiResponse<List<TableAvailabilityResponse>>> getAvailableTablesWithTimes(
            @RequestParam int numberOfPeople,
            @RequestParam String date,
            @RequestParam(required = false, defaultValue = "true") boolean filterByCapacity
    ) {
        LocalDateTime dateTime = LocalDate.parse(date).atStartOfDay();
        List<TableAvailabilityResponse> tables = tableService.getTablesWithAvailableTimes(numberOfPeople, dateTime, filterByCapacity);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesas y horarios disponibles", tables));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<TableResponse>>> getAvailableTables(
            @RequestParam int numberOfPeople,
            @RequestParam String startTime
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesas disponibles",
                tableService.getAvailableTables(numberOfPeople, startTime)));
    }

    @GetMapping("/optimal")
    public ResponseEntity<ApiResponse<TableResponse>> getOptimalTable(
            @RequestParam int numberOfPeople,
            @RequestParam String startTime
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesa óptima encontrada",
                tableService.findOptimalTable(numberOfPeople, startTime)));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Boolean>> checkTableAvailability(
            @PathVariable Long id,
            @RequestParam String startTime,
            @RequestParam int numberOfPeople,
            @RequestParam(required = false, defaultValue = "true") boolean includeBuffers
    ) {
        boolean available = tableService.isTableAvailable(id, startTime, numberOfPeople, includeBuffers);
        return ResponseEntity.ok(new ApiResponse<>(true, "Disponibilidad verificada", available));
    }

    /* -------------------- CRUD -------------------- */

    @GetMapping
    public ResponseEntity<ApiResponse<List<TableResponse>>> getAllTables() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesas obtenidas correctamente",
                tableService.getAllTables()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TableResponse>> getTableById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesa obtenida correctamente",
                tableService.getTableById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TableResponse>> createTable(@RequestBody @Valid TableRequest table) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesa creada correctamente",
                tableService.createTable(table)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TableResponse>> updateTable(@PathVariable Long id,
                                                                  @RequestBody @Valid TableRequest table) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesa actualizada correctamente",
                tableService.updateTable(id, table)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesa eliminada correctamente", null));
    }

}
