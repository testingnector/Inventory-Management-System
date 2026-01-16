package com.nector.auth.exception;

import org.springframework.http.HttpStatus;

public class OrgServiceException extends RuntimeException {

    private HttpStatus httpStatus;

    public OrgServiceException(String message, HttpStatus httpStatus, Exception exception) {
        super(message, exception);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

