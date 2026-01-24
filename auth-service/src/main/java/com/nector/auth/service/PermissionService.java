package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.internal.PermissionCreateRequest;
import com.nector.auth.dto.request.internal.PermissionUpdateRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.PermissionResponse;

import jakarta.validation.Valid;

public interface PermissionService {

	ApiResponse<PermissionResponse> createPermission(@Valid PermissionCreateRequest request,
			Authentication authentication);

	ApiResponse<PermissionResponse> updatePermission(UUID permissionId, @Valid PermissionUpdateRequest request,
			Authentication authentication);

	ApiResponse<List<Object>> deletePermission(UUID permissionId, Authentication authentication);

	ApiResponse<List<PermissionResponse>> fetchAllPermission();

	ApiResponse<PermissionResponse> getSinglePermission(UUID permissionId);

}
