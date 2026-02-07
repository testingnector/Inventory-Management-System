package com.nector.auth.controller.internal;

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

import com.nector.auth.dto.request.internal.RolePermissionAssignRequest;
import com.nector.auth.dto.request.internal.RolePermissionRevokeRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.PermissionRolesResponse;
import com.nector.auth.dto.response.internal.RolePermissionsResponse;
import com.nector.auth.service.RolePermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {

	private final RolePermissionService rolePermissionService;

	@PostMapping("/assign")
	public ResponseEntity<ApiResponse<RolePermissionsResponse>> assignPermissionToRole(
			@RequestBody RolePermissionAssignRequest rolePermissionAssignRequest, Authentication authentication) {

		ApiResponse<RolePermissionsResponse> response = rolePermissionService
				.assignPermission(rolePermissionAssignRequest, authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/revoke")
	public ResponseEntity<ApiResponse<RolePermissionsResponse>> revokePermission(
			@Valid @RequestBody RolePermissionRevokeRequest request, Authentication authentication) {

		ApiResponse<RolePermissionsResponse> response = rolePermissionService.revokePermission(request, authentication);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/role/{roleId}")
	public ResponseEntity<ApiResponse<RolePermissionsResponse>> getPermissionsByRoleId(@PathVariable UUID roleId) {

		ApiResponse<RolePermissionsResponse> response = rolePermissionService.getPermissionsByRole(roleId);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
	
	@GetMapping("/permission/{permissionId}")
	public ResponseEntity<ApiResponse<PermissionRolesResponse>> getPermissionsByPermissionId(@PathVariable UUID permissionId) {
		
		ApiResponse<PermissionRolesResponse> response = rolePermissionService.getRolePermissionsByPermissionId(permissionId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
	
	@GetMapping
	public ResponseEntity<ApiResponse<List<RolePermissionsResponse>>> getAllRolePermissions() {
		
		ApiResponse<List<RolePermissionsResponse>> response = rolePermissionService.getAllRolePermissions();
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
