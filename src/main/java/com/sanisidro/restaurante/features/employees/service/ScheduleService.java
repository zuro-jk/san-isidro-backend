package com.sanisidro.restaurante.features.employees.service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.features.employees.dto.schedule.request.ScheduleRequest;
import com.sanisidro.restaurante.features.employees.dto.schedule.response.ScheduleResponse;
import com.sanisidro.restaurante.features.employees.enums.DayOfWeekEnum;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.model.Schedule;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import com.sanisidro.restaurante.features.employees.repository.ScheduleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

        private final ScheduleRepository scheduleRepository;
        private final EmployeeRepository employeeRepository;
        private static final ZoneId RESTAURANT_ZONE_ID = ZoneId.of("America/Lima");

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
                                                () -> new EntityNotFoundException("Empleado no encontrado con id: "
                                                                + request.getEmployeeId()));

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
                                                () -> new EntityNotFoundException("Empleado no encontrado con id: "
                                                                + request.getEmployeeId()));

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

        public boolean isWithinSchedule(Employee employee) {

                ZonedDateTime nowZoned = ZonedDateTime.now(RESTAURANT_ZONE_ID);
                LocalTime now = nowZoned.toLocalTime();
                java.time.DayOfWeek javaDay = nowZoned.getDayOfWeek();
                DayOfWeekEnum dayOfWeek;

                log.info("--- Verificación de Horario para Employee ID: {} ---", employee.getId());
                log.info("Hora actual (America/Lima): {} | Día: {}", now, javaDay.name());

                try {
                        dayOfWeek = DayOfWeekEnum.valueOf(javaDay.name());
                } catch (IllegalArgumentException e) {
                        log.error("Error al mapear el día de la semana: {}", javaDay.name());
                        return false;
                }

                var scheduleOpt = employee.getSchedules().stream()
                                .filter(s -> s.getDayOfWeek() == dayOfWeek)
                                .findFirst();

                if (scheduleOpt.isEmpty()) {
                        log.warn("No se encontró horario para el día {}", dayOfWeek);
                        log.info("-----------------------------------------------------");
                        return false;
                }

                Schedule todaySchedule = scheduleOpt.get();
                LocalTime startTime = todaySchedule.getStartTime();
                LocalTime endTime = todaySchedule.getEndTime();

                log.info("Horario encontrado para {}: [{} - {}]", dayOfWeek, startTime, endTime);

                boolean onOrAfterStart = !now.isBefore(startTime);
                boolean onOrBeforeEnd = !now.isAfter(endTime);

                boolean result = onOrAfterStart && onOrBeforeEnd;

                log.info("Comprobación: (now >= startTime) -> {} | (now <= endTime) -> {}", onOrAfterStart,
                                onOrBeforeEnd);
                log.info("Resultado final de la comprobación: {}", result);
                log.info("-----------------------------------------------------");

                return result;
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
