package com.sanisidro.restaurante.features.restaurant.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<TableResponse>>> getAllTables() {
        List<TableResponse> tables = tableService.getAllTables();
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesas obtenidas correctamente", tables));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TableResponse>> getTableById(@PathVariable Long id) {
        TableResponse table = tableService.getTableById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesa obtenida correctamente", table));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TableResponse>> createTable(@RequestBody @Valid TableRequest table) {
        TableResponse created = tableService.createTable(table);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesa creada correctamente", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TableResponse>> updateTable(@PathVariable Long id,
                                                                  @RequestBody @Valid TableRequest table) {
        TableResponse updated = tableService.updateTable(id, table);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesa actualizada correctamente", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mesa eliminada correctamente", null));
    }
}
