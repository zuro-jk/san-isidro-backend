package com.sanisidro.restaurante.features.employees.errors;

public class OutOfScheduleAccessException extends RuntimeException {
    public OutOfScheduleAccessException(String message) {
        super(message);
    }
}