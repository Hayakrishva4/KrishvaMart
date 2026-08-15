package com.krishva.krishvamart.exception;

/** Thrown when a requested entity does not exist. Maps to HTTP 404. */
public class NotFoundException extends AppException {

    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
