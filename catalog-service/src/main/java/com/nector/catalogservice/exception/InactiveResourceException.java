package com.nector.catalogservice.exception;


import java.util.Map;

public class InactiveResourceException extends RuntimeException {

    private final Map<String, String> data;

    public InactiveResourceException(String resourceName) {
    	super("Inactive Resource Exception!");
        this.data = Map.of("Exception message", resourceName);
    }

    public Map<String, String> getData() {
        return data;
    }
}
