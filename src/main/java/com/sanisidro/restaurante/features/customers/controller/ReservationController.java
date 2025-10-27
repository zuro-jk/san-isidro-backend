package com.sanisidro.restaurante.features.customers.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.customers.dto.reservation.request.AuthenticatedReservationRequest;
import com.sanisidro.restaurante.features.customers.dto.reservation.request.ReservationRequest;
import com.sanisidro.restaurante.features.customers.dto.reservation.response.ReservationResponse;
import com.sanisidro.restaurante.features.customers.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagedResponse<ReservationResponse>>> getMyReservations(Pageable pageable) {
        PagedResponse<ReservationResponse> result = reservationService.getReservationsForAuthenticatedUser(pageable);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Reservas del usuario autenticado obtenidas correctamente", result));
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<PagedResponse<ReservationResponse>>> getAllReservations(Pageable pageable) {
        PagedResponse<ReservationResponse> result = reservationService.getAllReservations(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Todas las reservas obtenidas correctamente", result));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<PagedResponse<ReservationResponse>>> getReservationsByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {

        PagedResponse<ReservationResponse> result = reservationService.getReservationsByDateRange(startDate, endDate,
                pageable);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Reservas obtenidas por rango de fechas correctamente", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(@PathVariable Long id) {
        ReservationResponse reservation = reservationService.getReservation(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reserva obtenida correctamente", reservation));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<PagedResponse<ReservationResponse>>> getReservationsByCustomer(
            @PathVariable Long customerId,
            Pageable pageable) {
        PagedResponse<ReservationResponse> result = reservationService.getReservationsByCustomer(customerId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reservas obtenidas correctamente", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody ReservationRequest dto) {
        ReservationResponse reservation = reservationService.createReservation(dto);
        return new ResponseEntity<>(new ApiResponse<>(true, "Reserva creada correctamente", reservation),
                HttpStatus.CREATED);
    }

    @PostMapping("/me")
    public ResponseEntity<ReservationResponse> createMyReservation(
            @Valid @RequestBody AuthenticatedReservationRequest dto) {
        return ResponseEntity.ok(reservationService.createReservationForAuthenticatedUser(dto));
    }

    @PostMapping("/walkin/manual")
    public ResponseEntity<ApiResponse<ReservationResponse>> createWalkInReservation(
            @RequestParam Long customerId,
            @RequestParam(required = false) Long tableId,
            @RequestParam int numberOfPeople,
            @RequestParam(defaultValue = "true") boolean sendEmail // opcional
    ) {
        ReservationResponse reservation = reservationService.createWalkInReservation(customerId, tableId,
                numberOfPeople, sendEmail);
        return new ResponseEntity<>(new ApiResponse<>(true, "Walk-in creado correctamente (manual)", reservation),
                HttpStatus.CREATED);
    }

    @PostMapping("/walkin/auto")
    public ResponseEntity<ApiResponse<ReservationResponse>> createAutoWalkInReservation(
            @RequestParam Long customerId,
            @RequestParam int numberOfPeople,
            @RequestParam(defaultValue = "true") boolean sendEmail // opcional, default true
    ) {
        ReservationResponse reservation = reservationService.createAutoWalkInReservation(customerId, numberOfPeople,
                sendEmail);
        return new ResponseEntity<>(new ApiResponse<>(true, "Walk-in creado correctamente (automático)", reservation),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest dto) {
        ReservationResponse reservation = reservationService.updateReservation(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reserva actualizada correctamente", reservation));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirmReservation(@PathVariable Long id) {
        ReservationResponse reservation = reservationService.confirmReservation(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reserva confirmada correctamente", reservation));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<ReservationResponse>> completeReservation(@PathVariable Long id) {
        ReservationResponse reservation = reservationService.completeReservation(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reserva completada correctamente", reservation));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(@PathVariable Long id) {
        ReservationResponse reservation = reservationService.cancelReservation(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reserva cancelada correctamente", reservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reserva eliminada correctamente", null));
    }
}
