package com.sanisidro.restaurante.core.exceptions;

public class AddressAlreadyExistsException extends ConflictException {
    public AddressAlreadyExistsException(String message) {
        super(message);
    }
}
