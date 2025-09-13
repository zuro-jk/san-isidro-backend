package com.sanisidro.restaurante.core.exceptions;

public class CustomerAlreadyExistsException extends ConflictException {
    public CustomerAlreadyExistsException(String message) {
        super(message);
    }
}
