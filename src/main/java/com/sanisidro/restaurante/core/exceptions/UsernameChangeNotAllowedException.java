package com.sanisidro.restaurante.core.exceptions;

public class UsernameChangeNotAllowedException extends RuntimeException {
    public UsernameChangeNotAllowedException(String message) {
        super(message);
    }
}
