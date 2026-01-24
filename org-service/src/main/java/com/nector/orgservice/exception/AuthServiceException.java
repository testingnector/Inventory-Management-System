package com.nector.orgservice.exception;

import org.springframework.http.HttpStatus;

public class AuthServiceException extends RuntimeException {

    private HttpStatus httpStatus;

    public AuthServiceException(String message, HttpStatus httpStatus, Exception exception) {
        super(message, exception);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

