package com.handoffos.common.exception;

/** Throw from a service when the request clashes with existing data (e.g. duplicate name). Becomes HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
