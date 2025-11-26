package com.servicedesk.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceDeskException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public ServiceDeskException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "INTERNAL_ERROR";
    }

    public ServiceDeskException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = "ERROR";
    }

    public ServiceDeskException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public ServiceDeskException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "INTERNAL_ERROR";
    }

    public ServiceDeskException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = "ERROR";
    }
}
