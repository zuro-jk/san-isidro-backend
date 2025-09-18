package com.sanisidro.restaurante.features.employees.service;

import com.sanisidro.restaurante.core.audit.service.AuditLogService;
import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.employees.dto.employee.request.EmployeeRequest;
import com.sanisidro.restaurante.features.employees.dto.employee.response.EmployeeResponse;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.model.Position;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import com.sanisidro.restaurante.features.employees.repository.PositionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;

    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PagedResponse<EmployeeResponse> getAllPaged(Pageable pageable) {
        Page<Employee> page = employeeRepository.findAll(pageable);

        List<EmployeeResponse> content = page.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

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
        Employee employee = findByIdOrThrow(id);
        return mapToResponse(employee);
    }


    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + request.getUserId()));

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new EntityNotFoundException("Puesto no encontrado con id: " + request.getPositionId()));

        // sincronizar roles base sin perder extras (asume que User.syncRolesWithPosition está implementado)
        user.syncRolesWithPosition(position);
        userRepository.save(user);

        Employee employee = Employee.builder()
                .user(user)
                .position(position)
                .salary(request.getSalary())
                .hireDate(request.getHireDate())
                .status(request.getStatus())
                .build();

        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findByIdOrThrow(id);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + request.getUserId()));

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new EntityNotFoundException("Puesto no encontrado con id: " + request.getPositionId()));

        // sincronizar roles base sin perder extras
        user.syncRolesWithPosition(position);
        userRepository.save(user);

        employee.setUser(user);
        employee.setPosition(position);
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus());

        Employee updated = employeeRepository.save(employee);
        return mapToResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Employee employee = findByIdOrThrow(id);

        User user = employee.getUser();
        if (user != null && employee.getPosition() != null && employee.getPosition().getRoles() != null) {
            user.getRoles().removeAll(employee.getPosition().getRoles());
            userRepository.save(user);
        }

        employeeRepository.delete(employee);
    }

    private Employee findByIdOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + id));
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        Position pos = employee.getPosition();
        User user = employee.getUser();

        return EmployeeResponse.builder()
                .id(employee.getId())
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : null)
                .fullName(user != null ? user.getFullName() : null)
                .positionName(pos != null ? pos.getName() : null)
                .positionDescription(pos != null ? pos.getDescription() : null)
                .salary(employee.getSalary())
                .status(employee.getStatus() != null ? employee.getStatus().name() : null)
                .hireDate(employee.getHireDate())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .createdBy(employee.getCreatedBy())
                .updatedBy(employee.getUpdatedBy())
                .build();
    }
}
