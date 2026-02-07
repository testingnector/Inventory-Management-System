package com.nector.auth.controller.internal;

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

import com.nector.auth.dto.request.internal.UserRoleAssignRequest;
import com.nector.auth.dto.request.internal.UserRoleRevokeRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.RoleCompaniesResponse;
import com.nector.auth.dto.response.internal.UserCompaniesResponse;
import com.nector.auth.dto.response.internal.UserCompanyResponse;
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
	public ResponseEntity<ApiResponse<UserCompaniesResponse>> assignRole(
			@Valid @RequestBody UserRoleAssignRequest request, Authentication authentication) {

		ApiResponse<UserCompaniesResponse> response = userRoleService.assignRole(request, authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	// 🔹 REVOKE ROLE
	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@PutMapping("/revoke")
	public ResponseEntity<ApiResponse<UserCompaniesResponse>> revokeRole(
			@Valid @RequestBody UserRoleRevokeRequest request, Authentication authentication) {

		ApiResponse<UserCompaniesResponse> response = userRoleService.revokeRole(request, authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	// 🔹 Find All Users Roles
	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@GetMapping
	public ResponseEntity<ApiResponse<List<UserCompaniesResponse>>> fetchAllUsersRoles() {

		ApiResponse<List<UserCompaniesResponse>> response = userRoleService.getAllUsersRoles();
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@GetMapping("/roles/{roleId}")
	public ResponseEntity<ApiResponse<RoleCompaniesResponse>> getUserRolesByRoleId(@PathVariable UUID roleId) {
		ApiResponse<RoleCompaniesResponse> response = userRoleService.getUserRolesByRoleId(roleId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@GetMapping("/users/{userId}")
	public ResponseEntity<ApiResponse<UserCompaniesResponse>> getUserRolesByUserId(@PathVariable UUID userId) {
		ApiResponse<UserCompaniesResponse> response = userRoleService.getUserRolesByUserId(userId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@GetMapping("/users-companyid/{userId}")
	public ResponseEntity<ApiResponse<UserCompanyResponse>> getUserRolesByUserIdAndCompanyId(
			@PathVariable UUID userId, @RequestParam UUID companyId) {

		ApiResponse<UserCompanyResponse> response = userRoleService.getUserRolesByUserIdAndCompanyId(userId,
				companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
	
}
