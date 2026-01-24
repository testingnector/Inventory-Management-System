package com.nector.auth.dto.response.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private String httpStatus;   // change from String to HttpStatus

    private int httpStatusCode;      // change from String to int

    private T data;
}
