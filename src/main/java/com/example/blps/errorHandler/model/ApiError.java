package com.example.blps.errorHandler.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiError {
    private int status;
    private String message;
    private String debugMessage;

    public ApiError(int status, String message, String debugMessage) {
        this.status = status;
        this.message = message;
        this.debugMessage = debugMessage;
    }

    // Getters and setters
}
