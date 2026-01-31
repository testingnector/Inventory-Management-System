package com.nector.catalogservice.exception;

import java.util.Map;

public class DuplicateResourceException extends RuntimeException {

    private final Map<String, String> data;

    public DuplicateResourceException(String message) {
        super("Duplicate Resource Exception!");
        this.data = Map.of("Exception message", message); 
    }

    public Map<String, String> getData() {
        return data;
    }
}
