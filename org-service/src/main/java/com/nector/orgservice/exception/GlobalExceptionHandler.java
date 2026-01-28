package com.nector.orgservice.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nector.orgservice.dto.response.internal.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// ---------------- Validation errors ----------------
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
			MethodArgumentNotValidException exception) {

		Map<String, String> errors = new HashMap<>();
		exception.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String message = error.getDefaultMessage();
			errors.put(fieldName, message);
		});

		ApiResponse<Map<String, String>> response = new ApiResponse<>(false, "Validation failed!",
				HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(), errors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}
	
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
	
	@ExceptionHandler(AuthServiceException.class)
	public ResponseEntity<ApiResponse<Object>> handleAuthServiceException(AuthServiceException exception) {

		ApiResponse<Object> response = new ApiResponse<>(false, exception.getMessage(), exception.getHttpStatus().name(), exception.getHttpStatus().value(), Collections.emptyList());
		return ResponseEntity.status(exception.getHttpStatus()).body(response);
	}
	
	@ExceptionHandler(InactiveResourceException.class)
	public ResponseEntity<ApiResponse<Object>> handleInactiveResourceException(InactiveResourceException exception) {

		ApiResponse<Object> response = new ApiResponse<>(false, exception.getMessage(), HttpStatus.FORBIDDEN.name(), HttpStatus.FORBIDDEN.value(), exception.getData());
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
}
