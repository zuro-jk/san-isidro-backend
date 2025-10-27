package com.sanisidro.restaurante.features.orders.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.features.orders.dto.order.request.OrderRequest;
import com.sanisidro.restaurante.features.orders.dto.order.response.OrderResponse;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentInOrderRequest;
import com.sanisidro.restaurante.features.orders.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

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

    @GetMapping("/{id}/tracking")
    public ResponseEntity<ApiResponse<OrderResponse>> trackOrder(
            @PathVariable Long id,
            @RequestHeader(name = "Accept-Language", defaultValue = "es") String lang) {
        OrderResponse trackingInfo = orderService.getTrackingInfo(id, lang);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Seguimiento de la orden obtenido correctamente", trackingInfo));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @RequestHeader(name = "Accept-Language", defaultValue = "es") String lang,
            @AuthenticationPrincipal User user) {
        List<OrderResponse> orders = orderService.getOrdersForCurrentUser(user, lang);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Órdenes del usuario autenticado obtenidas correctamente", orders));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(name = "Accept-Language", defaultValue = "es") String lang,
            @AuthenticationPrincipal User user
            ) {
        OrderResponse order = orderService.create(user, request, lang);
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

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestHeader(name = "Accept-Language", defaultValue = "es") String lang) {
        OrderResponse cancelledOrder = orderService.cancelOrder(id, user, lang);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orden cancelada correctamente", cancelledOrder));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orden eliminada correctamente", null));
    }

    @PostMapping("/{id}/payments/local")
    public ResponseEntity<ApiResponse<Void>> addLocalPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentInOrderRequest request) {
        orderService.addLocalPayment(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Pago local registrado correctamente", null));
    }
}