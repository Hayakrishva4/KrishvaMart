package com.krishva.krishvamart.exception;

/** Wraps low-level java.sql.SQLException failures from the DAO layer. Maps to HTTP 500. */
public class DataAccessException extends AppException {

    public DataAccessException(String message, Throwable cause) {
        super("DATA_ACCESS_ERROR", message, cause);
    }
}
