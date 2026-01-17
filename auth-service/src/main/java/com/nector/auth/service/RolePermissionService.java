package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.RolePermissionAssignRequest;
import com.nector.auth.dto.request.RolePermissionRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.PermissionResponse;
import com.nector.auth.dto.response.RolePermissionResponse;

import jakarta.validation.Valid;

public interface RolePermissionService {

	ApiResponse<List<RolePermissionResponse>> assignPermission(RolePermissionAssignRequest rolePermissionAssignRequest, Authentication authentication);

	ApiResponse<RolePermissionResponse> revokePermission(@Valid RolePermissionRevokeRequest request,
			Authentication authentication);

	ApiResponse<List<RolePermissionResponse>> getPermissionsByRole(UUID roleId);

	ApiResponse<List<RolePermissionResponse>> getRolePermissionsByPermissionId(UUID permissionId);

}
