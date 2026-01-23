package com.nector.orgservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nector.orgservice.dto.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	
	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ApiResponse<Object>> handleDuplicateResourceException(DuplicateResourceException exception) {
		
		ApiResponse<Object> response = new ApiResponse<>(false, exception.getMessage(), HttpStatus.CONFLICT.name(), HttpStatus.CONFLICT.value(), exception.getData());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException exception) {

		ApiResponse<Object> response = new ApiResponse<>(false, exception.getMessage(), HttpStatus.NOT_FOUND.name(), HttpStatus.NOT_FOUND.value(), exception.getData());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}
}
