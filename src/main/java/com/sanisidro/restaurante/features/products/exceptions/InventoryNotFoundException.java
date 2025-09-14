package com.sanisidro.restaurante.features.products.exceptions;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String message) {
        super(message);
    }
}
