package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.exceptions.ResourceNotFoundException;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.features.customers.dto.address.request.AddressAdminRequest;
import com.sanisidro.restaurante.features.customers.dto.address.request.AddressCustomerRequest;
import com.sanisidro.restaurante.features.customers.dto.address.response.AddressResponse;
import com.sanisidro.restaurante.features.customers.model.Address;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.repository.AddressRepository;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public AddressResponse createAddressForAdmin(AddressAdminRequest dto) {
        Customer customer = findCustomerById(dto.getCustomerId());
        return createAddressInternal(customer, dto.getStreet(), dto.getReference(),
                dto.getCity(), dto.getProvince(), dto.getZipCode(), dto.getInstructions());
    }

    @Transactional
    public AddressResponse createAddressForCustomer(AddressCustomerRequest dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Customer customer = customerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado para el usuario autenticado"));

        return createAddressInternal(customer,
                dto.getStreet(), dto.getReference(),
                dto.getCity(), dto.getProvince(),
                dto.getZipCode(), dto.getInstructions());
    }


    @Transactional(readOnly = true)
    public AddressResponse getAddress(Long id) {
        Address address = findAddressById(id);
        return mapToResponse(address);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AddressResponse> getAddressesByCustomerAuth(User user, Pageable pageable) {
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado para el usuario autenticado"));

        Page<Address> page = addressRepository.findByCustomerId(customer.getId(), pageable);
        List<AddressResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return buildPagedResponse(page, content);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AddressResponse> getAllAddresses( Pageable pageable) {
        Page<Address> page = addressRepository.findAll(pageable);
        List<AddressResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return buildPagedResponse(page, content);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AddressResponse> getAddressesByCustomerId(Long customerId, Pageable pageable) {
        Page<Address> page = addressRepository.findByCustomerId(customerId, pageable);
        List<AddressResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return buildPagedResponse(page, content);
    }

    @Transactional
    public AddressResponse updateAddress(Long id, AddressAdminRequest dto) {
        Address address = findAddressById(id);

        address.updateFromDto(dto);
        Address updated = addressRepository.save(address);

        log.info("Dirección {} actualizada para cliente {}", updated.getId(), updated.getCustomer().getId());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteAddress(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        Customer customer = address.getCustomer();

        customer.getAddresses().remove(address);
    }


    private AddressResponse createAddressInternal(Customer customer,
                                                  String street, String reference,
                                                  String city, String province,
                                                  String zipCode, String instructions) {
        Address address = Address.builder()
                .customer(customer)
                .street(street)
                .reference(reference)
                .city(city)
                .province(province)
                .zipCode(zipCode)
                .instructions(instructions)
                .build();

        Address saved = addressRepository.save(address);
        return mapToResponse(saved);
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    private Address findAddressById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));
    }


    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .customerId(address.getCustomer().getId())
                .street(address.getStreet())
                .reference(address.getReference())
                .city(address.getCity())
                .province(address.getProvince())
                .zipCode(address.getZipCode())
                .instructions(address.getInstructions())
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
