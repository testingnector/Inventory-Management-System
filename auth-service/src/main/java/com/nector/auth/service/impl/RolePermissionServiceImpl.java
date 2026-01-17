package com.nector.auth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.nector.auth.dto.request.RolePermissionAssignRequest;
import com.nector.auth.dto.request.RolePermissionRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.RolePermissionResponse;
import com.nector.auth.entity.Permission;
import com.nector.auth.entity.Role;
import com.nector.auth.entity.RolePermission;
import com.nector.auth.entity.User;
import com.nector.auth.exception.ResourceNotFoundException;
import com.nector.auth.repository.PermissionRepository;
import com.nector.auth.repository.RolePermissionRepository;
import com.nector.auth.repository.RoleRepository;
import com.nector.auth.repository.UserRepository;
import com.nector.auth.service.RolePermissionService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

	private final RolePermissionRepository rolePermissionRepository;
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PermissionRepository permissionRepository;

	private UUID getLoggedInUserId(Authentication auth) {
		String email = auth.getName(); // JWT sub = email
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return user.getId();
	}

	@Transactional
	@Override
	public ApiResponse<List<RolePermissionResponse>> assignPermission(RolePermissionAssignRequest request,
			Authentication authentication) {

		Role role = roleRepository.findById(request.getRoleId())
				.orElseThrow(() -> new ResourceNotFoundException("Role not found"));

		UUID assignedBy = getLoggedInUserId(authentication);

		for (UUID permissionId : request.getPermissionIds()) {

			RolePermission rolePermission = rolePermissionRepository
					.findByRoleIdAndPermissionId(request.getRoleId(), permissionId).orElse(null);

			if (rolePermission == null) {
				rolePermission = new RolePermission();
				rolePermission.setRoleId(request.getRoleId());
				rolePermission.setPermissionId(permissionId);
				rolePermission.setActive(true);
				rolePermission.setAssignedBy(assignedBy);
				rolePermission.setCreatedBy(assignedBy);
				rolePermission.setAssignedAt(LocalDateTime.now());

			} else if (!rolePermission.getActive()) {
				rolePermission.setActive(true);
				rolePermission.setAssignedAt(LocalDateTime.now());
				rolePermission.setAssignedBy(assignedBy);
				rolePermission.setRevokedAt(null);
				rolePermission.setRevokedBy(null);
			}

			rolePermissionRepository.save(rolePermission);
		}

		ApiResponse<List<RolePermissionResponse>> response = getPermissionsByRole(request.getRoleId());

		response.setMessage("Permission assigned successfully");
		response.setHttpStatus(HttpStatus.CREATED.name());
		response.setHttpStatusCode(HttpStatus.CREATED.value());

		return response;

	}

	@Transactional
	@Override
	public ApiResponse<RolePermissionResponse> revokePermission(RolePermissionRevokeRequest request,
			Authentication authentication) {

		UUID revokedBy = getLoggedInUserId(authentication);

		RolePermission rolePermission = rolePermissionRepository
				.findByRoleIdAndPermissionId(request.getRoleId(), request.getPermissionId())
				.orElseThrow(() -> new ResourceNotFoundException("Role-Permission mapping not found"));

		if (!rolePermission.getActive()) {
			throw new RuntimeException("Permission already revoked");
		}

		rolePermission.setActive(false);
		rolePermission.setRevokedAt(LocalDateTime.now());
		rolePermission.setRevokedBy(revokedBy);

		rolePermissionRepository.save(rolePermission);

		List<RolePermissionResponse> list = getPermissionsByRole(request.getRoleId()).getData();

		for (RolePermissionResponse r : list) {
			if (r.getPermissionId().equals(request.getPermissionId())) {
				return new ApiResponse<>(true, "Permission revoked successfully", HttpStatus.OK.name(),
						HttpStatus.OK.value(), r);
			}
		}

		throw new ResourceNotFoundException("Updated permission not found");
	}

	@Override
	public ApiResponse<List<RolePermissionResponse>> getPermissionsByRole(UUID roleId) {

		List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleId);

		if (rolePermissions.isEmpty()) {
			throw new ResourceNotFoundException("No permissions found for this role");
		}

		Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found!"));

		List<UUID> permissionIds = new ArrayList<>();

		for (RolePermission rp : rolePermissions) {
			permissionIds.add(rp.getPermissionId());
		}

		List<Permission> permissions = permissionRepository.findAllById(permissionIds);
		Map<UUID, Permission> permissionMap = new HashMap<>();
		for (Permission permission : permissions) {
			permissionMap.put(permission.getId(), permission);
		}

		List<RolePermissionResponse> finalResponseList = new ArrayList<>();
		for (RolePermission rp : rolePermissions) {
			RolePermissionResponse response = mapToResponseWithRole(rp, role, permissionMap);
			finalResponseList.add(response);
		}

		return new ApiResponse<List<RolePermissionResponse>>(true, "Role permissions fetched successfully",
				HttpStatus.OK.name(), HttpStatus.OK.value(), finalResponseList);

	}

	private RolePermissionResponse mapToResponseWithRole(RolePermission rp, Role role,
			Map<UUID, Permission> permissionMap) {

		RolePermissionResponse rolePermissionResponse = new RolePermissionResponse();

		rolePermissionResponse.setRoleId(role.getId());
		rolePermissionResponse.setRoleCode(role.getRoleCode());
		rolePermissionResponse.setRoleName(role.getRoleName());

		Permission permission = permissionMap.get(rp.getPermissionId());
		rolePermissionResponse.setPermissionId(permission.getId());
		rolePermissionResponse.setPermissionCode(permission.getPermissionCode());
		rolePermissionResponse.setPermissionName(permission.getPermissionName());
		rolePermissionResponse.setModuleName(permission.getModuleName());

		rolePermissionResponse.setActive(rp.getActive());

		return rolePermissionResponse;
	}

//	==================================================================================

	@Override
	public ApiResponse<List<RolePermissionResponse>> getRolePermissionsByPermissionId(UUID permissionId) {

		List<RolePermission> rolePermissions = rolePermissionRepository.findByPermissionId(permissionId);

		if (rolePermissions.isEmpty()) {
			throw new ResourceNotFoundException("No role-permissions found for this permission");
		}

		Permission permission = permissionRepository.findById(permissionId).orElseThrow(
				() -> new ResourceNotFoundException("Permission is not found for this permission id" + permissionId));

		List<UUID> roleIds = new ArrayList<>();
		for (RolePermission rp : rolePermissions) {
			roleIds.add(rp.getRoleId());
		}

		List<Role> roles = roleRepository.findAllById(roleIds);
		Map<UUID, Role> roleMap = new HashMap<>();
		for (Role role : roles) {
			roleMap.put(role.getId(), role);
		}
		
		List<RolePermissionResponse> finalResponseList = new ArrayList<>();
		for (RolePermission rp : rolePermissions) {
			RolePermissionResponse response = mapToResponseWithPermission(rp, permission, roleMap);
			finalResponseList.add(response);
		}
		return new ApiResponse<List<RolePermissionResponse>>(true, "Role permissions fetched successfully",
				HttpStatus.OK.name(), HttpStatus.OK.value(), finalResponseList);
	}

	private RolePermissionResponse mapToResponseWithPermission(RolePermission rp, Permission permission, Map<UUID, Role> roleMap) {

		RolePermissionResponse rolePermissionResponse = new RolePermissionResponse();
		
		rolePermissionResponse.setPermissionId(permission.getId());
		rolePermissionResponse.setPermissionCode(permission.getPermissionCode());
		rolePermissionResponse.setPermissionName(permission.getPermissionName());
		rolePermissionResponse.setModuleName(permission.getModuleName());
		
		Role role = roleMap.get(rp.getRoleId());
		rolePermissionResponse.setRoleId(role.getId());
		rolePermissionResponse.setRoleCode(role.getRoleCode());
		rolePermissionResponse.setRoleName(role.getRoleName());
		
		rolePermissionResponse.setActive(rp.getActive());
		
		return rolePermissionResponse;
		
	}
}
