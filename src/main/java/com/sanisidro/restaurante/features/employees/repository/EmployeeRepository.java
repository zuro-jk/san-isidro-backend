package com.sanisidro.restaurante.features.employees.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanisidro.restaurante.features.employees.model.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByPositionNameNotIgnoreCase(String positionName);

    Optional<Employee> findByUserId(Long userId);
}
