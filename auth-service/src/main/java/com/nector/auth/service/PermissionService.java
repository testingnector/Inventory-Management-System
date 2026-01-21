package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.PermissionCreateRequest;
import com.nector.auth.dto.request.PermissionUpdateRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.PermissionResponses;

import jakarta.validation.Valid;

public interface PermissionService {

	ApiResponse<PermissionResponses> createPermission(@Valid PermissionCreateRequest request,
			Authentication authentication);

	ApiResponse<PermissionResponses> updatePermission(UUID permissionId, @Valid PermissionUpdateRequest request,
			Authentication authentication);

	ApiResponse<List<Object>> deletePermission(UUID permissionId, Authentication authentication);

	ApiResponse<List<PermissionResponses>> fetchAllPermission();

	ApiResponse<PermissionResponses> getSinglePermission(UUID permissionId);

}
