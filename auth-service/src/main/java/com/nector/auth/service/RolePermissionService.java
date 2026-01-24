package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.internal.RolePermissionAssignRequest;
import com.nector.auth.dto.request.internal.RolePermissionRevokeRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.PermissionRolesResponseDto1;
import com.nector.auth.dto.response.internal.RolePermissionsResponseDto1;

import jakarta.validation.Valid;

public interface RolePermissionService {

	ApiResponse<RolePermissionsResponseDto1> assignPermission(RolePermissionAssignRequest rolePermissionAssignRequest, Authentication authentication);

	ApiResponse<RolePermissionsResponseDto1> revokePermission(@Valid RolePermissionRevokeRequest request,
			Authentication authentication);

	ApiResponse<RolePermissionsResponseDto1> getPermissionsByRole(UUID roleId);

	ApiResponse<PermissionRolesResponseDto1> getRolePermissionsByPermissionId(UUID permissionId);

	ApiResponse<List<RolePermissionsResponseDto1>> getAllRolePermissions();

}
