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

import com.nector.auth.dto.request.internal.UserPermissionAssignRequest;
import com.nector.auth.dto.request.internal.UserPermissionRevokeRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.PermissionUsersResponseDto1;
import com.nector.auth.dto.response.internal.UserPermissionsResponseDto1;
import com.nector.auth.service.UserPermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("user-permissions")
@RequiredArgsConstructor
public class UserPermissionController {

	private final UserPermissionService userPermissionService;

	@PostMapping("/assign")
	public ResponseEntity<ApiResponse<UserPermissionsResponseDto1>> assign(
			@Valid @RequestBody UserPermissionAssignRequest userPermissionAssignRequest, Authentication authentication) {

		ApiResponse<UserPermissionsResponseDto1> response = userPermissionService.assignOrUpdate(userPermissionAssignRequest,
				authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/revoke")
	public ResponseEntity<ApiResponse<UserPermissionsResponseDto1>> revokePermission(
			@Valid @RequestBody UserPermissionRevokeRequest request, Authentication authentication) {
		ApiResponse<UserPermissionsResponseDto1> response = userPermissionService.revokeUserPermission(request,
				authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);

	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<ApiResponse<UserPermissionsResponseDto1>> getUserPermissionsByUser(@PathVariable UUID userId) {
		ApiResponse<UserPermissionsResponseDto1> response = userPermissionService.getUserPermissionsByUserId(userId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		
	}
	
	@GetMapping("/permission/{permissionId}")
	public ResponseEntity<ApiResponse<PermissionUsersResponseDto1>> getUserPermissionsByPermission(@PathVariable UUID permissionId) {
		ApiResponse<PermissionUsersResponseDto1> response = userPermissionService.getUserPermissionsByPermissionId(permissionId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		
	}
	
	@GetMapping
	public ResponseEntity<ApiResponse<List<UserPermissionsResponseDto1>>> getAllUserPermissions() {
		ApiResponse<List<UserPermissionsResponseDto1>> response = userPermissionService.getAllUserPermissions();
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		
	}

}
