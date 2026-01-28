package com.nector.auth.controller.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nector.auth.dto.request.internal.PermissionCreateRequest;
import com.nector.auth.dto.request.internal.PermissionUpdateRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.PermissionResponse;
import com.nector.auth.service.PermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

	private final PermissionService permissionService;

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
			@Valid @RequestBody PermissionCreateRequest request, Authentication authentication) {

		ApiResponse<PermissionResponse> response = permissionService.createPermission(request, authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@PutMapping("/update/{permissionId}")
	public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(@PathVariable UUID permissionId,
			@Valid @RequestBody PermissionUpdateRequest request, Authentication authentication) {

		ApiResponse<PermissionResponse> response = permissionService.updatePermission(permissionId, request,
				authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@DeleteMapping("/delete/{permissionId}")
	public ResponseEntity<ApiResponse<List<Object>>> deletePermission(@PathVariable UUID permissionId,
			Authentication authentication) {

		ApiResponse<List<Object>> response = permissionService.deletePermission(permissionId,
				authentication);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
	@GetMapping
	public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermission() {
		
		ApiResponse<List<PermissionResponse>> response = permissionService.fetchAllPermission();
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
	
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
	@GetMapping("/{permissionId}")
	public ResponseEntity<ApiResponse<PermissionResponse>> getSinglePermission(@PathVariable UUID permissionId) {
		
		ApiResponse<PermissionResponse> response = permissionService.getSinglePermission(permissionId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
}
