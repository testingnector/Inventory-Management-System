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

import com.nector.auth.dto.request.UserPermissionRequest;
import com.nector.auth.dto.request.UserPermissionRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.UserPermissionResponse;
import com.nector.auth.service.UserPermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("user-permissions")
@RequiredArgsConstructor
public class UserPermissionController {

	private final UserPermissionService userPermissionService;

	@PostMapping("/assign")
	public ResponseEntity<ApiResponse<UserPermissionResponse>> assign(
			@Valid @RequestBody UserPermissionRequest userPermissionRequest, Authentication authentication) {

		ApiResponse<UserPermissionResponse> response = userPermissionService.assignOrUpdate(userPermissionRequest,
				authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/revoke")
	public ResponseEntity<ApiResponse<UserPermissionResponse>> revokePermission(
			@Valid @RequestBody UserPermissionRevokeRequest request, Authentication authentication) {
		ApiResponse<UserPermissionResponse> response = userPermissionService.revokeUserPermission(request,
				authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);

	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<ApiResponse<List<UserPermissionResponse>>> getUserPermissionsByUser(@PathVariable UUID userId) {
		ApiResponse<List<UserPermissionResponse>> response = userPermissionService.getUserPermissionsByUserId(userId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		
	}
	
	@GetMapping("/permission/{permissionId}")
	public ResponseEntity<ApiResponse<List<UserPermissionResponse>>> getUserPermissionsByPermission(@PathVariable UUID permissionId) {
		ApiResponse<List<UserPermissionResponse>> response = userPermissionService.getUserPermissionsByPermissionId(permissionId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		
	}

}
