package com.sanisidro.restaurante.features.customers.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.customers.dto.customer.request.CustomerRequest;
import com.sanisidro.restaurante.features.customers.dto.customer.response.CustomerResponse;
import com.sanisidro.restaurante.features.customers.dto.pointshistory.response.PointsHistoryResponse;
import com.sanisidro.restaurante.features.customers.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> getAllCustomers(Pageable pageable) {
        PagedResponse<CustomerResponse> response = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Clientes obtenidos correctamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable Long id) {
        CustomerResponse customer = customerService.getCustomer(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cliente obtenido correctamente", customer));
    }

    @GetMapping("/{id}/points")
    public ResponseEntity<ApiResponse<Integer>> getPoints(@PathVariable Long id) {
        int points = customerService.getPoints(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Puntos obtenidos correctamente", points));
    }

    @GetMapping("/{id}/points/history")
    public ResponseEntity<ApiResponse<PagedResponse<PointsHistoryResponse>>> getPointsHistory(
            @PathVariable Long id, Pageable pageable) {
        PagedResponse<PointsHistoryResponse> history = customerService.getPointsHistory(id, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Historial de puntos obtenido correctamente", history));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerRequest dto) {
        CustomerResponse customer = customerService.createCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Cliente creado correctamente", customer));
    }

    @PostMapping("/{id}/points/add")
    public ResponseEntity<ApiResponse<CustomerResponse>> addPoints(
            @PathVariable Long id, @RequestParam int points) {
        CustomerResponse customer = customerService.addPoints(id, points);
        return ResponseEntity.ok(new ApiResponse<>(true, "Puntos agregados correctamente", customer));
    }

    @PostMapping("/{id}/points/subtract")
    public ResponseEntity<ApiResponse<CustomerResponse>> subtractPoints(
            @PathVariable Long id, @RequestParam int points) {
        CustomerResponse customer = customerService.subtractPoints(id, points);
        return ResponseEntity.ok(new ApiResponse<>(true, "Puntos restados correctamente", customer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id, @Valid @RequestBody CustomerRequest dto) {
        CustomerResponse customer = customerService.updateCustomer(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cliente actualizado correctamente", customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cliente eliminado correctamente", null));
    }

}
