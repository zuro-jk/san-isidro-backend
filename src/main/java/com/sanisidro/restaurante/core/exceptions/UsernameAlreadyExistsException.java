package com.sanisidro.restaurante.core.exceptions;

public class UsernameAlreadyExistsException extends ConflictException{
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
