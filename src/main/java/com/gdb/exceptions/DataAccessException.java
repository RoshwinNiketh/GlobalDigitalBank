package com.gdb.exceptions;

/** Wraps database access failures while keeping SQL details out of service code. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
