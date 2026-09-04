package com.krishva.krishvamart.exception;

public class DataAccessException extends AppException {
    public DataAccessException(String message, Throwable cause) {
        super("DATA_ACCESS_ERROR", message, cause);
    }
}
