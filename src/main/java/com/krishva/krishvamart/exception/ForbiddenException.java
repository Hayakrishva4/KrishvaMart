package com.krishva.krishvamart.exception;

public class ForbiddenException extends AppException {
    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }
}
