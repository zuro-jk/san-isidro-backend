package com.sanisidro.restaurante.core.exceptions;


public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message) {
        super(message);
    }
}
