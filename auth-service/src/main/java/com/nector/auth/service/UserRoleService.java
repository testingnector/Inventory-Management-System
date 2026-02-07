package com.nector.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.nector.auth.dto.request.internal.UserRoleAssignRequest;
import com.nector.auth.dto.request.internal.UserRoleRevokeRequest;
import com.nector.auth.dto.response.external.CompanyUsersResponseExternalDto;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.RoleCompaniesResponse;
import com.nector.auth.dto.response.internal.UserCompaniesResponse;
import com.nector.auth.dto.response.internal.UserCompanyResponse;

import jakarta.validation.Valid;

public interface UserRoleService {

	ApiResponse<UserCompaniesResponse> assignRole(@Valid UserRoleAssignRequest request, Authentication authentication);

	ApiResponse<UserCompaniesResponse> revokeRole(@Valid UserRoleRevokeRequest request, Authentication authentication);

	ApiResponse<List<UserCompaniesResponse>> getAllUsersRoles();

	ApiResponse<RoleCompaniesResponse> getUserRolesByRoleId(UUID roleId);

	ApiResponse<UserCompaniesResponse> getUserRolesByUserId(UUID userId);
	
	ApiResponse<UserCompanyResponse> getUserRolesByUserIdAndCompanyId(UUID userId, UUID companyId);

	ApiResponse<List<CompanyUsersResponseExternalDto>> getAllUsersByCompanyId(UUID companyId);



}
