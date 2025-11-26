package com.servicedesk.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ServiceDeskException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public UnauthorizedException() {
        super("Unauthorized access", HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
