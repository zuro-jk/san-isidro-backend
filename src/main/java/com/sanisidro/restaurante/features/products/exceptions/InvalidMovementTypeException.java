package com.sanisidro.restaurante.features.products.exceptions;

public class InvalidMovementTypeException extends RuntimeException {
    public InvalidMovementTypeException(String message) {
        super(message);
    }
}
