package com.nector.auth.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.AssignRoleRequest;
import com.nector.auth.dto.request.RoleRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.UserRoleResponse;

import jakarta.validation.Valid;

public interface UserRoleService {

	ApiResponse<UserRoleResponse> assignRole(@Valid AssignRoleRequest request, Authentication authentication);

	ApiResponse<UserRoleResponse> revokeRole(@Valid RoleRevokeRequest request, Authentication authentication);

	ApiResponse<List<UserRoleResponse>> getAllUsersRoles();

}
