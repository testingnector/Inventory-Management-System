package com.nector.auth.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nector.auth.dto.request.UserRoleAssignRequest;
import com.nector.auth.dto.request.UserRoleRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.user_role.RoleCompaniesUsersResponseDto1;
import com.nector.auth.dto.response.user_role.UserCompaniesRolesResponseDto1;
import com.nector.auth.dto.response.user_role.UserCompanyRolesResponseDto1;
import com.nector.auth.dto.response.user_role.UsersCompaniesRolesResponseDto1;
import com.nector.auth.service.UserRoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user-roles")
@RequiredArgsConstructor
public class UserRoleController {

	private final UserRoleService userRoleService;

	// 🔹 ASSIGN ROLE
	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@PostMapping("/assign")
	public ResponseEntity<ApiResponse<UsersCompaniesRolesResponseDto1>> assignRole(
			@Valid @RequestBody UserRoleAssignRequest request, Authentication authentication) {

		ApiResponse<UsersCompaniesRolesResponseDto1> response = userRoleService.assignRole(request, authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	// 🔹 REVOKE ROLE
	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@PutMapping("/revoke")
	public ResponseEntity<ApiResponse<UsersCompaniesRolesResponseDto1>> revokeRole(
			@Valid @RequestBody UserRoleRevokeRequest request, Authentication authentication) {

		ApiResponse<UsersCompaniesRolesResponseDto1> response = userRoleService.revokeRole(request, authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	// 🔹 Find All Users Roles
	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@GetMapping
	public ResponseEntity<ApiResponse<List<UsersCompaniesRolesResponseDto1>>> fetchAllUsersRoles() {

		ApiResponse<List<UsersCompaniesRolesResponseDto1>> response = userRoleService.getAllUsersRoles();
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@GetMapping("/roles/{roleId}")
	public ResponseEntity<ApiResponse<RoleCompaniesUsersResponseDto1>> getUserRolesByRoleId(@PathVariable UUID roleId) {
		ApiResponse<RoleCompaniesUsersResponseDto1> response = userRoleService.getUserRolesByRoleId(roleId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@GetMapping("/users/{userId}")
	public ResponseEntity<ApiResponse<UserCompaniesRolesResponseDto1>> getUserRolesByUserId(@PathVariable UUID userId) {
		ApiResponse<UserCompaniesRolesResponseDto1> response = userRoleService.getUserRolesByUserId(userId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@GetMapping("/users-companyid/{userId}")
	public ResponseEntity<ApiResponse<UserCompanyRolesResponseDto1>> getUserRolesByUserIdAndCompanyId(
			@PathVariable UUID userId, @RequestParam UUID companyId) {

		ApiResponse<UserCompanyRolesResponseDto1> response = userRoleService.getUserRolesByUserIdAndCompanyId(userId,
				companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
