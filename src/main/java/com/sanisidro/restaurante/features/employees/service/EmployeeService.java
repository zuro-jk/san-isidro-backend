package com.sanisidro.restaurante.features.employees.service;

import com.sanisidro.restaurante.core.audit.service.AuditLogService;
import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.employees.dto.employee.request.EmployeeRequest;
import com.sanisidro.restaurante.features.employees.dto.employee.response.EmployeeResponse;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PagedResponse<EmployeeResponse> getAllPaged(Pageable pageable) {
        Page<Employee> page = employeeRepository.findAll(pageable);

        List<EmployeeResponse> content = page.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public EmployeeResponse getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + id));
        return mapToResponse(employee);
    }

    public EmployeeResponse create(EmployeeRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + request.getUserId()));

        Employee employee = Employee.builder()
                .user(user)
                .position(request.getPosition())
                .salary(request.getSalary())
                .build();

        Employee saved = employeeRepository.save(employee);

//        auditLogService.log(
//                "Employee",
//                saved.getId(),
//                "CREATE",
//                null,
//                saved,
//                user.getUsername()
//        );


        return mapToResponse(saved);
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + id));

        Employee oldState = Employee.builder()
                // .id(employee.getId())
                .user(employee.getUser())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .build();

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + request.getUserId()));

        employee.setUser(user);
        employee.setPosition(request.getPosition());
        employee.setSalary(request.getSalary());

        Employee updated = employeeRepository.save(employee);

//        auditLogService.log(
//                "Employee",
//                updated.getId(),
//                "UPDATE",
//                oldState,
//                updated,
//                user.getUsername()
//        );

        return mapToResponse(updated);
    }

    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + id));

        employeeRepository.delete(employee);
//        auditLogService.log(
//                "Employee",
//                employee.getId(),
//                "DELETE",
//                employee,
//                null,
//                employee.getUser().getUsername()
//        );
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .userId(employee.getUser().getId())
                .username(employee.getUser().getUsername())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .createdBy(employee.getCreatedBy())
                .updatedBy(employee.getUpdatedBy())
                .build();
    }
}
