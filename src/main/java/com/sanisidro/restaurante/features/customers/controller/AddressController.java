package com.sanisidro.restaurante.features.customers.controller;

import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.customers.dto.address.request.AddressRequest;
import com.sanisidro.restaurante.features.customers.dto.address.response.AddressResponse;
import com.sanisidro.restaurante.features.customers.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddress(@PathVariable Long id) {
        AddressResponse response = addressService.getAddress(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dirección obtenida", response));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<PagedResponse<AddressResponse>>> getAddressesByCustomer(
            @PathVariable Long customerId, Pageable pageable) {
        PagedResponse<AddressResponse> paged = addressService.getAddressesByCustomer(customerId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Direcciones obtenidas", paged));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @Valid @RequestBody AddressRequest dto) {
        AddressResponse response = addressService.createAddress(dto);
        return new ResponseEntity<>(new ApiResponse<>(true, "Dirección creada", response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest dto) {
        AddressResponse response = addressService.updateAddress(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dirección actualizada", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dirección eliminada", null));
    }
}
