package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.internal.RolePermissionAssignRequest;
import com.nector.auth.dto.request.internal.RolePermissionRevokeRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.PermissionRolesResponse;
import com.nector.auth.dto.response.internal.RolePermissionsResponse;

import jakarta.validation.Valid;

public interface RolePermissionService {

	ApiResponse<RolePermissionsResponse> assignPermission(RolePermissionAssignRequest rolePermissionAssignRequest, Authentication authentication);

	ApiResponse<RolePermissionsResponse> revokePermission(@Valid RolePermissionRevokeRequest request,
			Authentication authentication);

	ApiResponse<RolePermissionsResponse> getPermissionsByRole(UUID roleId);

	ApiResponse<PermissionRolesResponse> getRolePermissionsByPermissionId(UUID permissionId);

	ApiResponse<List<RolePermissionsResponse>> getAllRolePermissions();

}
