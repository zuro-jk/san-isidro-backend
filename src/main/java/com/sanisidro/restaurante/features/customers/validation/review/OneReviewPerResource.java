package com.sanisidro.restaurante.features.customers.validation.review;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OneReviewPerResourceValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OneReviewPerResource {
    String message() default "El cliente ya ha realizado una reseña para este recurso";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}