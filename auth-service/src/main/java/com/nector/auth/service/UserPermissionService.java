package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.internal.UserPermissionAssignRequest;
import com.nector.auth.dto.request.internal.UserPermissionRevokeRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.PermissionUsersResponse;
import com.nector.auth.dto.response.internal.UserPermissionsResponse;

import jakarta.validation.Valid;

public interface UserPermissionService {

	ApiResponse<UserPermissionsResponse> assignOrUpdate(@Valid UserPermissionAssignRequest userPermissionAssignRequest, Authentication authentication);

	ApiResponse<UserPermissionsResponse> revokeUserPermission(@Valid UserPermissionRevokeRequest request, Authentication authentication);

	ApiResponse<UserPermissionsResponse> getUserPermissionsByUserId(UUID userId);

	ApiResponse<PermissionUsersResponse>  getUserPermissionsByPermissionId(UUID permissionId);

	ApiResponse<List<UserPermissionsResponse>> getAllUserPermissions();

}
