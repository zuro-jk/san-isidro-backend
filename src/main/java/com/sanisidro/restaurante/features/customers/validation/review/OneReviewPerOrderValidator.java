package com.sanisidro.restaurante.features.customers.validation.review;

import com.sanisidro.restaurante.features.customers.dto.review.request.ReviewRequest;
import com.sanisidro.restaurante.features.customers.repository.ReviewRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OneReviewPerOrderValidator implements ConstraintValidator<OneReviewPerOrder, ReviewRequest> {

    private final ReviewRepository reviewRepository;

    @Override
    public boolean isValid(ReviewRequest dto, ConstraintValidatorContext context) {
        if (dto.getCustomerId() == null || dto.getOrderId() == null) {
            return true;
        }
        return !reviewRepository.existsByCustomer_IdAndOrder_Id(dto.getCustomerId(), dto.getOrderId());
    }

}
