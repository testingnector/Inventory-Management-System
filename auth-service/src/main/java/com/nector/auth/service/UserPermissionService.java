package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.UserPermissionAssignRequest;
import com.nector.auth.dto.request.UserPermissionRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.user_permission.PermissionUsersGroupResponse;
import com.nector.auth.dto.response.user_permission.UserPermissionGroupResponse;
import com.nector.auth.dto.response.user_permission.UserPermissionResponse;

import jakarta.validation.Valid;

public interface UserPermissionService {

	ApiResponse<UserPermissionGroupResponse> assignOrUpdate(@Valid UserPermissionAssignRequest userPermissionAssignRequest, Authentication authentication);

	ApiResponse<UserPermissionGroupResponse> revokeUserPermission(@Valid UserPermissionRevokeRequest request, Authentication authentication);

	ApiResponse<UserPermissionGroupResponse> getUserPermissionsByUserId(UUID userId);

	ApiResponse<PermissionUsersGroupResponse>  getUserPermissionsByPermissionId(UUID permissionId);

	ApiResponse<List<UserPermissionGroupResponse>> getAllUserPermissions();

}
