package com.sanisidro.restaurante.features.customers.validation.resesrvation;

import com.sanisidro.restaurante.features.customers.dto.reservation.request.ReservationRequest;
import com.sanisidro.restaurante.features.customers.repository.ReservationRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueReservationValidator implements ConstraintValidator<UniqueReservation, ReservationRequest> {

    private final ReservationRepository reservationRepository;


    @Override
    public boolean isValid(ReservationRequest dto, ConstraintValidatorContext context) {
        if (dto.getCustomerId() == null || dto.getReservationDate() == null || dto.getReservationTime() == null) {
            return true;
        }
        return !reservationRepository.existsByCustomer_IdAndReservationDateAndReservationTime(
                dto.getCustomerId(),
                dto.getReservationDate(),
                dto.getReservationTime()
        );
    }
}
