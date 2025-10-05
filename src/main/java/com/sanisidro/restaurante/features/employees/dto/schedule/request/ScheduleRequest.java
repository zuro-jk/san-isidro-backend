package com.sanisidro.restaurante.features.employees.dto.schedule.request;

import java.time.LocalTime;

import com.sanisidro.restaurante.features.employees.enums.DayOfWeekEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRequest {

    @NotNull(message = "El id del empleado es obligatorio")
    private Long employeeId;

    @NotNull(message = "El día de la semana es obligatorio")
    private DayOfWeekEnum dayOfWeek;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;
}
