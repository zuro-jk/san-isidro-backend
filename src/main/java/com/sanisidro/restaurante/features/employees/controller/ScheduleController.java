package com.sanisidro.restaurante.features.employees.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.employees.dto.schedule.request.ScheduleRequest;
import com.sanisidro.restaurante.features.employees.dto.schedule.response.ScheduleResponse;
import com.sanisidro.restaurante.features.employees.service.ScheduleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getAll() {
        List<ScheduleResponse> schedules = scheduleService.getAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Horarios obtenidos correctamente", schedules));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getById(@PathVariable Long id) {
        ScheduleResponse schedule = scheduleService.getById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Horario obtenido correctamente", schedule));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> create(@Valid @RequestBody ScheduleRequest request) {
        ScheduleResponse schedule = scheduleService.create(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Horario creado correctamente", schedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> update(@PathVariable Long id,
            @Valid @RequestBody ScheduleRequest request) {
        ScheduleResponse schedule = scheduleService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Horario actualizado correctamente", schedule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Horario eliminado correctamente", null));
    }
}
