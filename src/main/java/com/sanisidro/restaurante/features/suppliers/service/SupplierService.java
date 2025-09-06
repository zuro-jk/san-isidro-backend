package com.sanisidro.restaurante.features.suppliers.service;

import com.sanisidro.restaurante.features.suppliers.dto.supplier.request.SupplierRequest;
import com.sanisidro.restaurante.features.suppliers.dto.supplier.response.SupplierResponse;
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

    public List<SupplierResponse> getAll() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SupplierResponse getById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado con id: " + id));
        return mapToResponse(supplier);
    }

    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .contact(request.getContact())
                .address(request.getAddress())
                .build();
        return mapToResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado con id: " + id));

        supplier.setName(request.getName());
        supplier.setContact(request.getContact());
        supplier.setAddress(request.getAddress());

        return mapToResponse(supplierRepository.save(supplier));
    }

    public void delete(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new EntityNotFoundException("Proveedor no encontrado con id: " + id);
        }
        supplierRepository.deleteById(id);
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contact(supplier.getContact())
                .address(supplier.getAddress())
                .build();
    }

}
