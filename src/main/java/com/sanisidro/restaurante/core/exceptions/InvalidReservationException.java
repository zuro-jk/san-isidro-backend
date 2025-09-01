package com.sanisidro.restaurante.core.exceptions;

public class InvalidReservationException extends RuntimeException {
    public InvalidReservationException(String message) {
        super(message);
    }
}
