package com.example.blps.errorHandler;

// NotFoundException.java
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}