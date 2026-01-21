package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.RolePermissionAssignRequest;
import com.nector.auth.dto.request.RolePermissionRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.role_permission.PermissionRoleGroupResponse;
import com.nector.auth.dto.response.role_permission.RolePermissionGroupResponse;
import com.nector.auth.dto.response.role_permission.RolePermissionResponse;

import jakarta.validation.Valid;

public interface RolePermissionService {

	ApiResponse<RolePermissionGroupResponse> assignPermission(RolePermissionAssignRequest rolePermissionAssignRequest, Authentication authentication);

	ApiResponse<RolePermissionGroupResponse> revokePermission(@Valid RolePermissionRevokeRequest request,
			Authentication authentication);

	ApiResponse<RolePermissionGroupResponse> getPermissionsByRole(UUID roleId);

	ApiResponse<PermissionRoleGroupResponse> getRolePermissionsByPermissionId(UUID permissionId);

	ApiResponse<List<RolePermissionGroupResponse>> getAllRolePermissions();

}
