package com.nector.auth.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nector.auth.dto.request.internal.UserCreateRequest;
import com.nector.auth.dto.request.internal.UserUpdateRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.UserResponse;
import com.nector.auth.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/insert")
	public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequest request,
			Authentication authentication) {

		ApiResponse<?> response = userService.createUser(request, authentication);
		if (response.isSuccess()) {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		} else {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		}
	}

	// Update user
	@PutMapping("/update/{userId}")
	public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable UUID userId,
			@Valid @RequestBody UserUpdateRequest request, Authentication authentication) {

		ApiResponse<UserResponse> response = userService.updateUser(userId, request, authentication);
		if (response.isSuccess()) {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		} else {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		}
	}

	// Delete user
	@DeleteMapping("/delete/{userId}")
	public ResponseEntity<ApiResponse<List<Object>>> deleteUser(@PathVariable UUID userId, Authentication authentication) {

		ApiResponse<List<Object>> response = userService.deleteUser(userId, authentication);
		if (response.isSuccess()) {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		} else {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		}
	}

	// Fetch all users
	@GetMapping
	public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(Authentication authentication) {
		ApiResponse<List<UserResponse>> response = userService.getAllUsers(authentication);
		if (response.isSuccess()) {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		} else {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		}
	}
}
