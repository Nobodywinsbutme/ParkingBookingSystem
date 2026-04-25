package com.app.support;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a booking cannot be created due to overlapping time range or conflicting rules.
 */
public class BookingConflictException extends RuntimeException {
    private final HttpStatus status;

    public BookingConflictException(String message) {
        this(message, HttpStatus.CONFLICT);
    }

    public BookingConflictException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
