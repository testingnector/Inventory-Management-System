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

import com.nector.auth.dto.request.UserPermissionAssignRequest;
import com.nector.auth.dto.request.UserPermissionRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.user_permission.PermissionUsersGroupResponse;
import com.nector.auth.dto.response.user_permission.UserPermissionGroupResponse;
import com.nector.auth.dto.response.user_permission.UserPermissionResponse;
import com.nector.auth.service.UserPermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("user-permissions")
@RequiredArgsConstructor
public class UserPermissionController {

	private final UserPermissionService userPermissionService;

	@PostMapping("/assign")
	public ResponseEntity<ApiResponse<UserPermissionGroupResponse>> assign(
			@Valid @RequestBody UserPermissionAssignRequest userPermissionAssignRequest, Authentication authentication) {

		ApiResponse<UserPermissionGroupResponse> response = userPermissionService.assignOrUpdate(userPermissionAssignRequest,
				authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/revoke")
	public ResponseEntity<ApiResponse<UserPermissionGroupResponse>> revokePermission(
			@Valid @RequestBody UserPermissionRevokeRequest request, Authentication authentication) {
		ApiResponse<UserPermissionGroupResponse> response = userPermissionService.revokeUserPermission(request,
				authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);

	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<ApiResponse<UserPermissionGroupResponse>> getUserPermissionsByUser(@PathVariable UUID userId) {
		ApiResponse<UserPermissionGroupResponse> response = userPermissionService.getUserPermissionsByUserId(userId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		
	}
	
	@GetMapping("/permission/{permissionId}")
	public ResponseEntity<ApiResponse<PermissionUsersGroupResponse>> getUserPermissionsByPermission(@PathVariable UUID permissionId) {
		ApiResponse<PermissionUsersGroupResponse> response = userPermissionService.getUserPermissionsByPermissionId(permissionId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		
	}
	
	@GetMapping
	public ResponseEntity<ApiResponse<List<UserPermissionGroupResponse>>> getAllUserPermissions() {
		ApiResponse<List<UserPermissionGroupResponse>> response = userPermissionService.getAllUserPermissions();
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		
	}

}
