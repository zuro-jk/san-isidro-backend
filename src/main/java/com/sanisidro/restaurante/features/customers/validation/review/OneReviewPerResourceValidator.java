package com.sanisidro.restaurante.features.customers.validation.review;

import com.sanisidro.restaurante.features.customers.dto.review.request.ReviewRequest;
import com.sanisidro.restaurante.features.customers.repository.ReviewRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OneReviewPerResourceValidator implements ConstraintValidator<OneReviewPerResource, ReviewRequest> {

    private final ReviewRepository reviewRepository;

    @Override
    public boolean isValid(ReviewRequest dto, ConstraintValidatorContext context) {

        if (dto.getCustomerId() == null) {
            return true; // No hay cliente, otra validación debe manejarlo
        }

        // Validar review por orden
        if (dto.getOrderId() != null) {
            if (dto.getId() != null) {
                // Update
                return !reviewRepository.existsByCustomer_IdAndOrder_IdAndIdNot(dto.getCustomerId(), dto.getOrderId(), dto.getId());
            }
            return !reviewRepository.existsByCustomer_IdAndOrder_Id(dto.getCustomerId(), dto.getOrderId());
        }

        // Validar review por reserva
        if (dto.getReservationId() != null) {
            if (dto.getId() != null) {
                return !reviewRepository.existsByCustomer_IdAndReservation_IdAndIdNot(dto.getCustomerId(), dto.getReservationId(), dto.getId());
            }
            return !reviewRepository.existsByCustomer_IdAndReservation_Id(dto.getCustomerId(), dto.getReservationId());
        }

        // Validar review por producto
        if (dto.getProductId() != null) {
            if (dto.getId() != null) {
                return !reviewRepository.existsByCustomer_IdAndProduct_IdAndIdNot(dto.getCustomerId(), dto.getProductId(), dto.getId());
            }
            return !reviewRepository.existsByCustomer_IdAndProduct_Id(dto.getCustomerId(), dto.getProductId());
        }

        // Si no hay recurso definido, dejamos pasar
        return true;
    }
}