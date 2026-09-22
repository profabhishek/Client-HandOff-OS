package com.handoffos.common.exception;

/** Throw from a service when something doesn't exist (or belongs to another tenant). Becomes HTTP 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
