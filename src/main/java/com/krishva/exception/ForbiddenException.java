package com.krishva.krishvamart.exception;

/** Thrown when an authenticated user lacks permission for the action. Maps to HTTP 403. */
public class ForbiddenException extends AppException {

    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }
}
