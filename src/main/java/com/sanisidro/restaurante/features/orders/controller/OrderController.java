package com.sanisidro.restaurante.features.orders.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.orders.dto.order.request.OrderRequest;
import com.sanisidro.restaurante.features.orders.dto.order.response.OrderResponse;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentInOrderRequest;
import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.service.OrderService;
import com.sanisidro.restaurante.features.orders.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAll(
            @RequestHeader(name = "Accept-Language", defaultValue = "es") String lang) {
        List<OrderResponse> orders = orderService.getAll(lang);
        return ResponseEntity.ok(new ApiResponse<>(true, "Órdenes obtenidas correctamente", orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(
            @PathVariable Long id,
            @RequestHeader(name = "Accept-Language", defaultValue = "es") String lang) {
        OrderResponse order = orderService.getById(id, lang);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orden obtenida correctamente", order));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(name = "Accept-Language", defaultValue = "es") String lang) {
        OrderResponse order = orderService.create(request, lang);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orden creada correctamente", order));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(name = "Accept-Language", defaultValue = "es") String lang) {
        OrderResponse order = orderService.update(id, request, lang);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orden actualizada correctamente", order));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orden eliminada correctamente", null));
    }

    @PostMapping("/{id}/payments/local")
    public ResponseEntity<ApiResponse<Void>> addLocalPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentInOrderRequest request
    ) {
        orderService.addLocalPayment(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Pago local registrado correctamente", null));
    }
}