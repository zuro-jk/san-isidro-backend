package com.sanisidro.restaurante.features.employees.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.features.employees.dto.schedule.request.ScheduleRequest;
import com.sanisidro.restaurante.features.employees.dto.schedule.response.ScheduleResponse;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.model.Schedule;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import com.sanisidro.restaurante.features.employees.repository.ScheduleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAll() {
        return scheduleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getById(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Horario no encontrado con id: " + id));
        return mapToResponse(schedule);
    }

    @Transactional
    public ScheduleResponse create(ScheduleRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Empleado no encontrado con id: " + request.getEmployeeId()));

        Schedule schedule = Schedule.builder()
                .employee(employee)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        return mapToResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public ScheduleResponse update(Long id, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Horario no encontrado con id: " + id));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Empleado no encontrado con id: " + request.getEmployeeId()));

        schedule.setEmployee(employee);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());

        return mapToResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public void delete(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Horario no encontrado con id: " + id));
        scheduleRepository.delete(schedule);
    }

    private ScheduleResponse mapToResponse(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .employeeId(schedule.getEmployee().getId())
                .employeeName(schedule.getEmployee().getUser().getFullName())
                .positionName(schedule.getEmployee().getPosition().getName())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .build();
    }
}
