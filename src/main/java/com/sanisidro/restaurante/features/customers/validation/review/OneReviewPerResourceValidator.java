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
            return true;
        }

        int count = 0;
        if (dto.getOrderId() != null) count++;
        if (dto.getReservationId() != null) count++;
        if (dto.getProductId() != null) count++;
        if (count != 1) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Debe especificar exactamente uno: orderId, reservationId o productId"
            ).addConstraintViolation();
            return false;
        }

        if (dto.getOrderId() != null) {
            if (dto.getId() != null) {
                // Update
                return !reviewRepository.existsByCustomer_IdAndOrder_IdAndIdNot(
                        dto.getCustomerId(), dto.getOrderId(), dto.getId()
                );
            }
            return !reviewRepository.existsByCustomer_IdAndOrder_Id(dto.getCustomerId(), dto.getOrderId());
        }

        if (dto.getReservationId() != null) {
            if (dto.getId() != null) {
                return !reviewRepository.existsByCustomer_IdAndReservation_IdAndIdNot(
                        dto.getCustomerId(), dto.getReservationId(), dto.getId()
                );
            }
            return !reviewRepository.existsByCustomer_IdAndReservation_Id(
                    dto.getCustomerId(), dto.getReservationId()
            );
        }

        if (dto.getProductId() != null) {
            if (dto.getId() != null) {
                return !reviewRepository.existsByCustomer_IdAndProduct_IdAndIdNot(
                        dto.getCustomerId(), dto.getProductId(), dto.getId()
                );
            }
            return !reviewRepository.existsByCustomer_IdAndProduct_Id(
                    dto.getCustomerId(), dto.getProductId()
            );
        }

        return true;
    }
}