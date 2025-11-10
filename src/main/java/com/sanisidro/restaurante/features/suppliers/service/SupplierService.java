package com.sanisidro.restaurante.features.suppliers.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.exceptions.EmailAlreadyExistsException;
import com.sanisidro.restaurante.core.exceptions.UsernameAlreadyExistsException;
import com.sanisidro.restaurante.core.security.enums.AuthProvider;
import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.suppliers.dto.supplier.request.SupplierRequest;
import com.sanisidro.restaurante.features.suppliers.dto.supplier.response.SupplierResponse;
import com.sanisidro.restaurante.features.suppliers.exceptions.SupplierNotFoundException;
import com.sanisidro.restaurante.features.suppliers.model.Supplier;
import com.sanisidro.restaurante.features.suppliers.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<SupplierResponse> getAll() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SupplierResponse getById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Proveedor no encontrado con id: " + id));
        return mapToResponse(supplier);
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("El nombre de usuario ya existe");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya existe");
        }

        Role supplierRole = roleRepository.findByName("ROLE_SUPPLIER")
                .orElseThrow(() -> new RuntimeException("Rol ROLE_SUPPLIER no encontrado"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getContactName())
                .lastName(request.getCompanyName())
                .phone(request.getPhone())
                .roles(Set.of(supplierRole))
                .enabled(true)
                .emailVerified(true)
                .provider(AuthProvider.LOCAL)
                .build();

        User savedUser = userRepository.save(user);

        Supplier supplier = Supplier.builder()
                .companyName(request.getCompanyName())
                .contactName(request.getContactName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .user(savedUser)
                .build();

        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional // <-- AÑADIR
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = findByIdOrThrow(id);
        User user = supplier.getUser();

        supplier.setCompanyName(request.getCompanyName());
        supplier.setContactName(request.getContactName());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());

        if (user != null) {
            user.setFirstName(request.getContactName());
            user.setLastName(request.getCompanyName());
            user.setPhone(request.getPhone());

            // Validar email
            if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
                if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                    throw new EmailAlreadyExistsException("El email ya está en uso");
                }
                user.setEmail(request.getEmail());
            }

            // Validar username
            if (!user.getUsername().equals(request.getUsername())) {
                if (userRepository.existsByUsername(request.getUsername())) {
                    throw new UsernameAlreadyExistsException("El nombre de usuario ya existe");
                }
                user.setUsername(request.getUsername());
            }

            userRepository.save(user);
        }

        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional // <-- AÑADIR
    public void delete(Long id) {
        Supplier supplier = findByIdOrThrow(id);

        supplierRepository.delete(supplier);

        if (supplier.getUser() != null) {
            userRepository.delete(supplier.getUser());
        }
    }

    private Supplier findByIdOrThrow(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Proveedor no encontrado con id: " + id));
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        User user = supplier.getUser();

        return SupplierResponse.builder()
                .id(supplier.getId())
                .companyName(supplier.getCompanyName())
                .contactName(supplier.getContactName())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .build();
    }
}
