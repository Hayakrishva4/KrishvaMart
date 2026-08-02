package com.krishva.krishvamart.exception;

/** Thrown on a state conflict, e.g. duplicate email or insufficient stock. Maps to HTTP 409. */
public class ConflictException extends AppException {

    public ConflictException(String message) {
        super("CONFLICT", message);
    }
}
