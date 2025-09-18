package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.exceptions.AddressAlreadyExistsException;
import com.sanisidro.restaurante.core.exceptions.ResourceNotFoundException;
import com.sanisidro.restaurante.features.customers.dto.address.request.AddressRequest;
import com.sanisidro.restaurante.features.customers.dto.address.response.AddressResponse;
import com.sanisidro.restaurante.features.customers.model.Address;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.repository.AddressRepository;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public AddressResponse createAddress(AddressRequest dto) {
        Customer customer = findCustomerById(dto.getCustomerId());
        String normalizedAddress = normalizeAddress(dto.getAddress());

        if (addressRepository.existsByCustomerAndAddress(customer, normalizedAddress)) {
            throw new AddressAlreadyExistsException("El cliente ya tiene esta dirección registrada");
        }

        Address address = Address.builder()
                .customer(customer)
                .address(dto.getAddress().trim())
                .reference(dto.getReference())
                .build();

        Address saved = addressRepository.save(address);
        log.info("Dirección creada para cliente {}: {}", customer.getId(), dto.getAddress());

        return mapToResponse(saved);
    }


    @Transactional(readOnly = true)
    public AddressResponse getAddress(Long id) {
        Address address = findAddressById(id);
        return mapToResponse(address);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AddressResponse> getAddressesByCustomer(Long customerId, Pageable pageable) {
        Page<Address> page = addressRepository.findByCustomerId(customerId, pageable);
        List<AddressResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return buildPagedResponse(page, content);
    }

    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest dto) {
        Address address = findAddressById(id);

        if (dto.getAddress() != null) {
            String normalizedAddress = normalizeAddress(dto.getAddress());
            if (!normalizedAddress.equals(normalizeAddress(address.getAddress()))
                    && addressRepository.existsByCustomerAndAddress(address.getCustomer(), normalizedAddress)) {
                throw new AddressAlreadyExistsException("El cliente ya tiene esta dirección registrada");
            }
        }

        address.updateFromDto(dto);
        Address updated = addressRepository.save(address);

        log.info("Dirección {} actualizada para cliente {}", updated.getId(), updated.getCustomer().getId());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteAddress(Long id) {
        Address address = findAddressById(id);
        addressRepository.delete(address);
        log.info("Dirección {} eliminada para cliente {}", id, address.getCustomer().getId());
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    private String normalizeAddress(String address) {
        return address == null ? null : address.trim().toLowerCase();
    }

    private Address findAddressById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));
    }


    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .customerId(address.getCustomer().getId())
                .address(address.getAddress())
                .reference(address.getReference())
                .createdAt(address.getCreatedAt() != null ? address.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(address.getUpdatedAt() != null ? address.getUpdatedAt().format(FORMATTER) : null)
                .createdBy(address.getCreatedBy())
                .updatedBy(address.getUpdatedBy())
                .build();
    }

    private <T> PagedResponse<T> buildPagedResponse(Page<?> page, List<T> content) {
        return PagedResponse.<T>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
