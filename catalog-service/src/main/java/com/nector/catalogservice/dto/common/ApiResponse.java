package com.nector.catalogservice.dto.common;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	
    private boolean success;

    private String message;
   
    private String httpStatus;   

    private int httpStatusCode; 

    private String code;

    private T data;

    private List<ErrorDetail> errors;

    private Pagination pagination;

    private Map<String, Object> metadata;

    private String traceId;

    private String path;

    private String apiVersion;

    private Instant timestamp = Instant.now();
    

}