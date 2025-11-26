package com.servicedesk.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ServiceDeskException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public ForbiddenException() {
        super("Access denied", HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
