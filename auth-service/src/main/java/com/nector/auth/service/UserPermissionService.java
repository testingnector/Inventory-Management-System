package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.UserPermissionRequest;
import com.nector.auth.dto.request.UserPermissionRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.UserPermissionResponse;

import jakarta.validation.Valid;

public interface UserPermissionService {

	ApiResponse<UserPermissionResponse> assignOrUpdate(@Valid UserPermissionRequest userPermissionRequest, Authentication authentication);

	ApiResponse<UserPermissionResponse> revokeUserPermission(@Valid UserPermissionRevokeRequest request, Authentication authentication);

	ApiResponse<List<UserPermissionResponse>> getUserPermissionsByUserId(UUID userId);

	ApiResponse<List<UserPermissionResponse>> getUserPermissionsByPermissionId(UUID permissionId);

}
