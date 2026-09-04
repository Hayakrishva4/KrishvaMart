package com.krishva.krishvamart.exception;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super("UNAUTHENTICATED", message);
    }
}
