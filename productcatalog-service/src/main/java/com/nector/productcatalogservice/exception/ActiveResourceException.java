package com.nector.productcatalogservice.exception;


import java.util.Map;

public class ActiveResourceException extends RuntimeException {

    private final Map<String, String> data;

    public ActiveResourceException(String resourceName) {
        super(resourceName + " is inactive");  
        this.data = Map.of("Exception message", resourceName);
    }

    public Map<String, String> getData() {
        return data;
    }
}
