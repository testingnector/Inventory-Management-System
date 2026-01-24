package com.nector.auth.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nector.auth.dto.response.internal.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// ---------------- Email / Role exceptions ----------------

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<String>> handleAllException(Exception ex) {

		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // default

		// --------- Common Java / Spring exceptions ---------
		if (ex instanceof IllegalArgumentException) {
			status = HttpStatus.BAD_REQUEST; // 400
		} else if (ex instanceof NullPointerException) {
			status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
		} else if (ex instanceof IllegalStateException) {
			status = HttpStatus.CONFLICT; // 409
		} else if (ex instanceof UnsupportedOperationException) {
			status = HttpStatus.NOT_IMPLEMENTED; // 501
		} else if (ex instanceof IndexOutOfBoundsException || ex instanceof ArrayIndexOutOfBoundsException) {
			status = HttpStatus.BAD_REQUEST; // 400
		} else if (ex instanceof ClassCastException) {
			status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
		} else if (ex instanceof NumberFormatException) {
			status = HttpStatus.BAD_REQUEST; // 400
		} else if (ex instanceof SecurityException) {
			status = HttpStatus.FORBIDDEN; // 403
		} else if (ex instanceof UnsupportedOperationException) {
			status = HttpStatus.NOT_IMPLEMENTED; // 501
		} else if (ex instanceof org.springframework.dao.DataIntegrityViolationException) {
			status = HttpStatus.CONFLICT; // 409
		} else if (ex instanceof org.springframework.dao.EmptyResultDataAccessException) {
			status = HttpStatus.NOT_FOUND; // 404
		} else if (ex instanceof org.springframework.web.bind.MissingServletRequestParameterException) {
			status = HttpStatus.BAD_REQUEST; // 400
		} else if (ex instanceof org.springframework.web.method.annotation.MethodArgumentTypeMismatchException) {
			status = HttpStatus.BAD_REQUEST; // 400
		} else if (ex instanceof org.springframework.http.converter.HttpMessageNotReadableException) {
			status = HttpStatus.BAD_REQUEST; // 400
		} else if (ex instanceof org.springframework.web.HttpRequestMethodNotSupportedException) {
			status = HttpStatus.METHOD_NOT_ALLOWED; // 405
		} else if (ex instanceof org.springframework.security.authentication.BadCredentialsException) {
			status = HttpStatus.UNAUTHORIZED; // 401
		} else if (ex instanceof org.springframework.security.access.AccessDeniedException) {
			status = HttpStatus.FORBIDDEN; // 403
		}

		// Build the response dynamically
		ApiResponse<String> response = new ApiResponse<>(false, ex.getClass().getSimpleName(), // exception type
				status.name(), status.value(), ex.getMessage());

		return ResponseEntity.status(status).body(response);
	}

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

	// ---------------- Login / Authentication errors ----------------
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiResponse<String>> handleBadCredentials(BadCredentialsException exception) {

		ApiResponse<String> response = new ApiResponse<>(false, "Invalid Email or Password!", HttpStatus.UNAUTHORIZED.name(),
				HttpStatus.UNAUTHORIZED.value(), exception.getMessage());

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<ApiResponse<String>> handleUsernameNotFound(UsernameNotFoundException exception) {

		ApiResponse<String> response = new ApiResponse<>(false, "Email does not exist!", HttpStatus.UNAUTHORIZED.name(),
				HttpStatus.UNAUTHORIZED.value(), exception.getMessage());

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

//	===========================================================================================
	@ExceptionHandler(javax.naming.AuthenticationException.class)
	public ResponseEntity<ApiResponse<String>> handleAuthenticationException(
			javax.naming.AuthenticationException exception) {

		ApiResponse<String> response = new ApiResponse<>(false, "Authentication Failed!", HttpStatus.UNAUTHORIZED.name(),
				HttpStatus.UNAUTHORIZED.value(), exception.getMessage());

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

//	===========================================================================================
	@ExceptionHandler(OrgServiceException.class)
	public ResponseEntity<ApiResponse<Object>> handleOrgServiceException(OrgServiceException exception) {

		ApiResponse<Object> response = new ApiResponse<>(false, exception.getMessage(), exception.getHttpStatus().name(), exception.getHttpStatus().value(), Collections.emptyList());
		return ResponseEntity.status(exception.getHttpStatus()).body(response);
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

}
