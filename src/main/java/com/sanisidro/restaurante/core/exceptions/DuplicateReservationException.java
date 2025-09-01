package com.sanisidro.restaurante.core.exceptions;

public class DuplicateReservationException extends RuntimeException {
    public DuplicateReservationException(String message) {
        super(message);
    }
}
