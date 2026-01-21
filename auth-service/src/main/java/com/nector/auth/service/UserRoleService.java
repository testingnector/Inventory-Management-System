package com.nector.auth.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.UserRoleAssignRequest;
import com.nector.auth.dto.request.UserRoleRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.UserRoleResponse;

import jakarta.validation.Valid;

public interface UserRoleService {

	ApiResponse<UserRoleResponse> assignRole(@Valid UserRoleAssignRequest request, Authentication authentication);

	ApiResponse<UserRoleResponse> revokeRole(@Valid UserRoleRevokeRequest request, Authentication authentication);

	ApiResponse<List<UserRoleResponse>> getAllUsersRoles();

}
