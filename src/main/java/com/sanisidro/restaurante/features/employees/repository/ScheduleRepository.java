package com.sanisidro.restaurante.features.employees.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanisidro.restaurante.features.employees.model.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

}
