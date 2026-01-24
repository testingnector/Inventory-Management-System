package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.internal.RoleCreateRequest;
import com.nector.auth.dto.request.internal.RoleUpdateRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.RoleResponse;

import jakarta.validation.Valid;

public interface RoleService {

	ApiResponse<RoleResponse> createRole(@Valid RoleCreateRequest request, Authentication authentication);

	ApiResponse<RoleResponse> updateRole(UUID roleId, @Valid RoleUpdateRequest roleUpdateRequest, Authentication authentication);

	ApiResponse<List<Object>> deleteRole(UUID roleId, Authentication authentication);

	ApiResponse<List<RoleResponse>> getAllRoles();
}
