package com.nector.catalogservice.dto.response.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private String httpStatus;   

    private int httpStatusCode;     

    private T data;
}
