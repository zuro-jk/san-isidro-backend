package com.sanisidro.restaurante.features.suppliers.service;

import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.suppliers.dto.supplier.request.SupplierRequest;
import com.sanisidro.restaurante.features.suppliers.dto.supplier.response.SupplierResponse;
import com.sanisidro.restaurante.features.suppliers.exceptions.SupplierNotFoundException;
import com.sanisidro.restaurante.features.suppliers.model.Supplier;
import com.sanisidro.restaurante.features.suppliers.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

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

    public SupplierResponse create(SupplierRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SupplierNotFoundException("Usuario no encontrado con id: " + userId));

        Supplier supplier = Supplier.builder()
                .companyName(request.getCompanyName())
                .contactName(request.getContactName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .user(user)
                .build();

        return mapToResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Proveedor no encontrado con id: " + id));

        supplier.setCompanyName(request.getCompanyName());
        supplier.setContactName(request.getContactName());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());

        return mapToResponse(supplierRepository.save(supplier));
    }

    public void delete(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new SupplierNotFoundException("Proveedor no encontrado con id: " + id);
        }
        supplierRepository.deleteById(id);
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .companyName(supplier.getCompanyName())
                .contactName(supplier.getContactName())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .userId(supplier.getUser() != null ? supplier.getUser().getId() : null)
                .build();
    }

}
