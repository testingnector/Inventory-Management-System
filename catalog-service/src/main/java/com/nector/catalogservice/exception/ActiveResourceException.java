package com.nector.catalogservice.exception;


import java.util.Map;

public class ActiveResourceException extends RuntimeException {

    private final Map<String, String> data;

    public ActiveResourceException(String resourceName) {
    	 super("Active Resource Exception!");
        this.data = Map.of("Exception message", resourceName);
    }

    public Map<String, String> getData() {
        return data;
    }
}
