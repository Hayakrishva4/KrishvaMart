package com.krishva.krishvamart.exception;

/** Thrown when service-layer input validation fails. Maps to HTTP 400. */
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
