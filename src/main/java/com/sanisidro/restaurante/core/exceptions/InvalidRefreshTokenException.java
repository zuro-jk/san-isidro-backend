package com.sanisidro.restaurante.core.exceptions;

public class InvalidRefreshTokenException extends RuntimeException{

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
