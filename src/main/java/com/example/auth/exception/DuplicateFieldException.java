package com.example.auth.exception;

public class DuplicateFieldException extends RuntimeException {
    private final String field;

    public DuplicateFieldException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
