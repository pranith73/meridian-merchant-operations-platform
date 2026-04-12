package com.pranith73.meridian.shared.error;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}