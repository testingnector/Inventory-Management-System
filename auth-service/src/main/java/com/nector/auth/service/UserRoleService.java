package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.internal.UserRoleAssignRequest;
import com.nector.auth.dto.request.internal.UserRoleRevokeRequest;
import com.nector.auth.dto.response.external.CompanyUsersResponseExternalDto;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.RoleCompaniesUsersResponseDto1;
import com.nector.auth.dto.response.internal.UserCompaniesRolesResponseDto1;
import com.nector.auth.dto.response.internal.UserCompanyRolesResponseDto1;
import com.nector.auth.dto.response.internal.UsersCompaniesRolesResponseDto1;

import jakarta.validation.Valid;

public interface UserRoleService {

	ApiResponse<UsersCompaniesRolesResponseDto1> assignRole(@Valid UserRoleAssignRequest request, Authentication authentication);

	ApiResponse<UsersCompaniesRolesResponseDto1> revokeRole(@Valid UserRoleRevokeRequest request, Authentication authentication);

	ApiResponse<List<UsersCompaniesRolesResponseDto1>> getAllUsersRoles();

	ApiResponse<RoleCompaniesUsersResponseDto1> getUserRolesByRoleId(UUID roleId);

	ApiResponse<UserCompaniesRolesResponseDto1> getUserRolesByUserId(UUID userId);
	
	ApiResponse<UserCompanyRolesResponseDto1> getUserRolesByUserIdAndCompanyId(UUID userId, UUID companyId);

	ApiResponse<List<CompanyUsersResponseExternalDto>> getAllUsersByCompanyId(UUID companyId);



}
