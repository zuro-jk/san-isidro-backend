package com.sanisidro.restaurante.features.customers.validation.review;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OneReviewPerOrderValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OneReviewPerOrder {
    String message() default "El cliente ya ha realizado una reseña para este pedido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
