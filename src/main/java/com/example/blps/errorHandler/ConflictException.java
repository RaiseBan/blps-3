package com.example.blps.errorHandler;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}