package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.internal.UserPermissionAssignRequest;
import com.nector.auth.dto.request.internal.UserPermissionRevokeRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.PermissionUsersResponseDto1;
import com.nector.auth.dto.response.internal.UserPermissionsResponseDto1;

import jakarta.validation.Valid;

public interface UserPermissionService {

	ApiResponse<UserPermissionsResponseDto1> assignOrUpdate(@Valid UserPermissionAssignRequest userPermissionAssignRequest, Authentication authentication);

	ApiResponse<UserPermissionsResponseDto1> revokeUserPermission(@Valid UserPermissionRevokeRequest request, Authentication authentication);

	ApiResponse<UserPermissionsResponseDto1> getUserPermissionsByUserId(UUID userId);

	ApiResponse<PermissionUsersResponseDto1>  getUserPermissionsByPermissionId(UUID permissionId);

	ApiResponse<List<UserPermissionsResponseDto1>> getAllUserPermissions();

}
