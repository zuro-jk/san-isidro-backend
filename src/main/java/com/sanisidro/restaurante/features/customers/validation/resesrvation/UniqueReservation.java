package com.sanisidro.restaurante.features.customers.validation.resesrvation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueReservationValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueReservation {
    String message() default "El cliente ya tiene una reserva para esta fecha y hora";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
