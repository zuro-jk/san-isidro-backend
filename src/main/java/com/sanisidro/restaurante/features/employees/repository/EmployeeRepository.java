package com.sanisidro.restaurante.features.employees.repository;

import com.sanisidro.restaurante.features.employees.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
