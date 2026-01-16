package com.nector.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nector.auth.dto.request.AssignRoleRequest;
import com.nector.auth.dto.request.RoleRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.UserRoleResponse;
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
	public ResponseEntity<ApiResponse<UserRoleResponse>> assignRole(@Valid @RequestBody AssignRoleRequest request,
			Authentication authentication) {

		ApiResponse<UserRoleResponse> response = userRoleService.assignRole(request, authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	// 🔹 REVOKE ROLE
	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@PostMapping("/revoke")
	public ResponseEntity<ApiResponse<UserRoleResponse>> revokeRole(@Valid @RequestBody RoleRevokeRequest request,
			Authentication authentication) {

		ApiResponse<UserRoleResponse> response = userRoleService.revokeRole(request, authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	// 🔹 Find All Users Roles
	@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
	@GetMapping
	public ResponseEntity<ApiResponse<List<UserRoleResponse>>> fetchAllUsersRoles() {

		ApiResponse<List<UserRoleResponse>> response = userRoleService.getAllUsersRoles();
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
