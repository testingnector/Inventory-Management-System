package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.UserCreateRequest;
import com.nector.auth.dto.request.UserUpdateRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.UserResponse;

import jakarta.validation.Valid;

public interface UserService {

	ApiResponse<?> createUser(@Valid UserCreateRequest request, Authentication authentication);

	ApiResponse<UserResponse> updateUser(UUID userId, @Valid UserUpdateRequest request, Authentication authentication);

	ApiResponse<List<Object>> deleteUser(UUID userId, Authentication authentication);

	ApiResponse<List<UserResponse>> getAllUsers(Authentication authentication);

}
