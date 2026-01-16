package com.nector.auth.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nector.auth.dto.request.RolePermissionAssignRequest;
import com.nector.auth.dto.request.RolePermissionRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.RolePermissionResponse;
import com.nector.auth.service.RolePermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {

	private final RolePermissionService rolePermissionService;

	@PostMapping("/assign")
	public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> assignPermissionToRole(
			@RequestBody RolePermissionAssignRequest rolePermissionAssignRequest, Authentication authentication) {

		ApiResponse<List<RolePermissionResponse>> response = rolePermissionService
				.assignPermission(rolePermissionAssignRequest, authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/revoke")
	public ResponseEntity<ApiResponse<RolePermissionResponse>> revokePermission(
			@Valid @RequestBody RolePermissionRevokeRequest request, Authentication authentication) {

		ApiResponse<RolePermissionResponse> response = rolePermissionService.revokePermission(request, authentication);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/role/{roleId}")
	public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> getPermissionsByRoleId(@PathVariable UUID roleId) {

		ApiResponse<List<RolePermissionResponse>> response = rolePermissionService.getPermissionsByRole(roleId);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
