package com.sanisidro.restaurante.features.employees.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.aws.service.FileService;
import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.exceptions.EmailAlreadyExistsException;
import com.sanisidro.restaurante.core.exceptions.UsernameAlreadyExistsException;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.core.security.service.UserService;
import com.sanisidro.restaurante.features.employees.dto.employee.request.EmployeeRequest;
import com.sanisidro.restaurante.features.employees.dto.employee.response.EmployeeResponse;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.model.Position;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import com.sanisidro.restaurante.features.employees.repository.PositionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PositionRepository positionRepository;
    private final FileService fileService;
    private final UserService userService;

    public List<EmployeeResponse> getAll() {
        return employeeRepository.findByPositionNameNotIgnoreCase("SUPPLIER")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PagedResponse<EmployeeResponse> getAllPaged(Pageable pageable) {
        Page<Employee> page = employeeRepository.findAll(pageable);

        List<EmployeeResponse> content = page.getContent()
                .stream()
                .filter(e -> e.getPosition() != null
                        && !"SUPPLIER".equalsIgnoreCase(e.getPosition().getName()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                content.size(), // total filtrado
                (int) Math.ceil((double) content.size() / page.getSize()),
                page.isLast());
    }

    public EmployeeResponse getById(Long id) {
        Employee employee = findByIdOrThrow(id);
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria para crear un empleado.");
        }
        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }

        User user = userService.createNewUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone());

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Puesto no encontrado con id: " + request.getPositionId()));

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

        User user = employee.getUser();
        if (user == null) {
            throw new EntityNotFoundException("El empleado no tiene un usuario asociado.");
        }

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Puesto no encontrado con id: " + request.getPositionId()));

        employee.setPosition(position);
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus());

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        if (!user.getUsername().equals(request.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new UsernameAlreadyExistsException("El nombre de usuario ya está en uso.");
            }
            user.setUsername(request.getUsername());
        }
        
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new EmailAlreadyExistsException("El email ya está en uso.");
            }
            user.setEmail(request.getEmail());
        }

        user.syncRolesWithPosition(position);
        
        userRepository.save(user);
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

        String profileImageUrl = null;
        if (user.getProfileImageId() != null) {
            try {
                profileImageUrl = fileService.getFileUrl(user.getProfileImageId());
            } catch (Exception e) {
                profileImageUrl = null;
            }
        }

        return EmployeeResponse.builder()
                .id(employee.getId())
                .userId(user != null ? user.getId() : null)

                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .fullName(user != null ? user.getFullName() : null)
                .profileImageUrl(profileImageUrl)

                .positionId(pos != null ? pos.getId() : null)
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
