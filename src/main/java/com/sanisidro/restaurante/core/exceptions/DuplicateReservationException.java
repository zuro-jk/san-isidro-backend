package com.sanisidro.restaurante.core.exceptions;

public class DuplicateReservationException extends ConflictException {
    public DuplicateReservationException(String message) {
        super(message);
    }
}
