package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.features.customers.dto.address.request.AddressRequest;
import com.sanisidro.restaurante.features.customers.dto.address.response.AddressResponse;
import com.sanisidro.restaurante.features.customers.model.Address;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.repository.AddressRepository;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    public AddressResponse createAddress(AddressRequest dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Address address = Address.builder()
                .customer(customer)
                .address(dto.getAddress())
                .reference(dto.getReference())
                .build();

        return mapToResponse(addressRepository.save(address));
    }

    public AddressResponse getAddress(Long id) {
        return mapToResponse(addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada")));
    }

    public List<AddressResponse> getAddressesByCustomer(Long customerId) {
        return addressRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse updateAddress(Long id, AddressRequest dto) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        if (dto.getAddress() != null) address.setAddress(dto.getAddress());
        if (dto.getReference() != null) address.setReference(dto.getReference());

        return mapToResponse(addressRepository.save(address));
    }

    public void deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new RuntimeException("Address not found");
        }
        addressRepository.deleteById(id);
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .customerId(address.getCustomer().getId())
                .address(address.getAddress())
                .reference(address.getReference())
                .build();
    }
}
