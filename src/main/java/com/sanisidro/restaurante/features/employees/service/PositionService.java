package com.sanisidro.restaurante.features.employees.service;

import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.features.employees.dto.employee.request.PositionRequest;
import com.sanisidro.restaurante.features.employees.dto.employee.response.PositionResponse;
import com.sanisidro.restaurante.features.employees.model.Position;
import com.sanisidro.restaurante.features.employees.repository.PositionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;
    private final RoleRepository roleRepository;

    public PositionResponse getById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Puesto no encontrado con id: " + id));
        return mapToResponse(position);
    }

    public List<PositionResponse> getAll() {
        return positionRepository.findAll()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PagedResponse<PositionResponse> getAllPaged(Pageable pageable) {
        Page<Position> page = positionRepository.findAll(pageable);
        List<PositionResponse> responses = page.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                responses,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public PositionResponse create(PositionRequest request) {
        Set<Role> roles = roleRepository.findAllById(request.getRoleIds())
                .stream().collect(Collectors.toSet());

        Position position = Position.builder()
                .name(request.getName())
                .description(request.getDescription())
                .roles(roles)
                .build();

        Position saved = positionRepository.save(position);
        return mapToResponse(saved);
    }

    public PositionResponse update(Long id, PositionRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Puesto no encontrado con id: " + id));

        Set<Role> roles = roleRepository.findAllById(request.getRoleIds())
                .stream().collect(Collectors.toSet());

        position.setName(request.getName());
        position.setDescription(request.getDescription());
        position.setRoles(roles);

        Position updated = positionRepository.save(position);
        return mapToResponse(updated);
    }

    public void delete(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Puesto no encontrado con id: " + id));
        positionRepository.delete(position);
    }

    private PositionResponse mapToResponse(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .name(position.getName())
                .description(position.getDescription())
                .roles(
                        position.getRoles().stream()
                                .map(role -> PositionResponse.RoleSummary.builder()
                                        .id(role.getId())
                                        .name(role.getName())
                                        .build()
                                ).collect(Collectors.toSet())
                )
                .build();
    }

}
