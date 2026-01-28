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

import com.nector.auth.dto.request.internal.RoleCreateRequest;
import com.nector.auth.dto.request.internal.RoleUpdateRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.RoleResponse;
import com.nector.auth.service.RoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

	private final RoleService roleService;

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleCreateRequest request,
			Authentication authentication) {
		
		ApiResponse<RoleResponse> response = roleService.createRole(request, authentication);
		if (response.isSuccess()) {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		} else {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		}
		
	}

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@PutMapping("/update/{roleId}")
	public ResponseEntity<ApiResponse<RoleResponse>> updateRole(@PathVariable UUID roleId,
			@RequestBody @Valid RoleUpdateRequest roleUpdateRequest, Authentication authentication) {
		
		ApiResponse<RoleResponse> response = roleService.updateRole(roleId, roleUpdateRequest, authentication);
		if (response.isSuccess()) {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		} else {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		}
	
	}

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@DeleteMapping("/delete/{roleId}")
	public ResponseEntity<ApiResponse<List<Object>>> deleteRole(@PathVariable UUID roleId, Authentication authentication) {
		
		ApiResponse<List<Object>> response = roleService.deleteRole(roleId, authentication);
		if (response.isSuccess()) {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		} else {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		}
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles() {

		ApiResponse<List<RoleResponse>> response = roleService.getAllRoles();
		if (response.isSuccess()) {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		} else {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		}
	}
}
