package com.nector.orgservice.exception;

import java.util.Map;

public class ResourceNotFoundException extends RuntimeException {


    private final Map<String, String> data;

    public ResourceNotFoundException(String message) {
        super("Resource Not Found Exception!"); 
        this.data = Map.of("Exception message", message); 
    }

    public Map<String, String> getData() {
        return data;
    }
}
