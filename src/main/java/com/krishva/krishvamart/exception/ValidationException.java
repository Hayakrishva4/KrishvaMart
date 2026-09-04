package com.krishva.krishvamart.exception;

public class ValidationException extends AppException {
    private final String field;
    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
