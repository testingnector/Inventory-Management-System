package com.nector.auth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.nector.auth.dto.request.internal.RolePermissionAssignRequest;
import com.nector.auth.dto.request.internal.RolePermissionRevokeRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.AssignedPermissionWithRolePermissionResponse;
import com.nector.auth.dto.response.internal.AssignedRoleResponse;
import com.nector.auth.dto.response.internal.PermissionRolesResponse;
import com.nector.auth.dto.response.internal.RolePermissionsResponse;
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
		String email = auth.getName(); 
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return user.getId();
	}

	@Transactional
	@Override
	public ApiResponse<RolePermissionsResponse> assignPermission(RolePermissionAssignRequest request,
			Authentication authentication) {

		UUID assignedBy = getLoggedInUserId(authentication);

		Role role = roleRepository.findById(request.getRoleId())
				.orElseThrow(() -> new ResourceNotFoundException("Role not found"));

		List<RolePermission> existingRolePermissions = rolePermissionRepository
				.findByRoleIdAndPermissionIdIn(request.getRoleId(), request.getPermissionIds());

		Map<UUID, RolePermission> existingMap = existingRolePermissions.stream()
				.collect(Collectors.toMap(RolePermission::getPermissionId, rp -> rp));

		List<RolePermission> rolePermissionsToSave = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();

		for (UUID permissionId : request.getPermissionIds()) {
			RolePermission rp = existingMap.get(permissionId);

			if (rp == null) {
				rp = new RolePermission();
				rp.setRoleId(request.getRoleId());
				rp.setPermissionId(permissionId);
				rp.setActive(true);
				rp.setAssignedBy(assignedBy);
				rp.setCreatedBy(assignedBy);
				rp.setAssignedAt(now);
			} else if (!rp.getActive()) {
				// Reactivate
				rp.setActive(true);
				rp.setAssignedAt(now);
				rp.setAssignedBy(assignedBy);
				rp.setRevokedAt(null);
				rp.setRevokedBy(null);
			}

			rolePermissionsToSave.add(rp);
		}

		rolePermissionRepository.saveAll(rolePermissionsToSave);

		List<Permission> permissions = permissionRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(
				request.getPermissionIds().stream().collect(Collectors.toSet()));

		List<AssignedPermissionWithRolePermissionResponse> permissionResponses = new ArrayList<>();
		for (Permission p : permissions) {

			RolePermission rp = existingMap.get(p.getId());
			if (rp == null) {
				for (RolePermission r : rolePermissionsToSave) {
					if (r.getPermissionId().equals(p.getId())) {
						rp = r;
						break;
					}
				}
			}

			AssignedPermissionWithRolePermissionResponse rpr = new AssignedPermissionWithRolePermissionResponse();
			rpr.setPermissionId(p.getId());
			rpr.setPermissionCode(p.getPermissionCode());
			rpr.setPermissionName(p.getPermissionName());
			rpr.setPermissionDescription(p.getDescription());
			rpr.setModuleName(p.getModuleName());
			rpr.setPermissionActive(p.getActive());

			rpr.setAllowed(rp != null && rp.getActive());
			rpr.setAssignedActive(rp != null ? rp.getActive() : false);
			rpr.setAssignedAt(rp != null ? rp.getAssignedAt() : null);

			permissionResponses.add(rpr);
		}

		RolePermissionsResponse response = new RolePermissionsResponse();
		response.setRoleId(role.getId());
		response.setRoleCode(role.getRoleCode());
		response.setRoleName(role.getRoleName());
		response.setRoleDescription(role.getDescription());
		response.setSystemRole(role.getSystemRole());
		response.setActive(role.getActive());
		response.setPermissions(permissionResponses);

		return new ApiResponse<>(true, "Permissions assigned successfully", HttpStatus.CREATED.name(),
				HttpStatus.CREATED.value(), response);
	}

	@Transactional
	@Override
	public ApiResponse<RolePermissionsResponse> revokePermission(RolePermissionRevokeRequest request,
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

		List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(request.getRoleId());
		if (rolePermissions.isEmpty()) {
			throw new ResourceNotFoundException("No permissions found for this role");
		}

		Role role = roleRepository.findById(request.getRoleId())
				.orElseThrow(() -> new ResourceNotFoundException("Role not found!"));

		List<UUID> permissionIds = new ArrayList<>();
		for (RolePermission rp : rolePermissions) {
			permissionIds.add(rp.getPermissionId());
		}

		List<Permission> permissions = permissionRepository.findAllById(permissionIds);
		Map<UUID, Permission> permissionMap = new HashMap<>();
		for (Permission p : permissions) {
			permissionMap.put(p.getId(), p);
		}

		List<AssignedPermissionWithRolePermissionResponse> permissionResponses = new ArrayList<>();
		for (RolePermission rp : rolePermissions) {
			Permission permission = permissionMap.get(rp.getPermissionId());
			if (permission == null)
				continue;

			AssignedPermissionWithRolePermissionResponse rpr = new AssignedPermissionWithRolePermissionResponse();
			rpr.setPermissionId(permission.getId());
			rpr.setPermissionCode(permission.getPermissionCode());
			rpr.setPermissionName(permission.getPermissionName());
			rpr.setPermissionDescription(permission.getDescription());
			rpr.setModuleName(permission.getModuleName());
			rpr.setPermissionActive(permission.getActive());

			rpr.setAllowed(rp.getActive());
			rpr.setAssignedActive(rp.getActive());
			rpr.setAssignedAt(rp.getAssignedAt());

			permissionResponses.add(rpr);
		}

		RolePermissionsResponse response = new RolePermissionsResponse();
		response.setRoleId(role.getId());
		response.setRoleCode(role.getRoleCode());
		response.setRoleName(role.getRoleName());
		response.setRoleDescription(role.getDescription());
		response.setSystemRole(role.getSystemRole());
		response.setActive(role.getActive());
		response.setPermissions(permissionResponses);

		return new ApiResponse<>(true, "Permission revoked successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				response);
	}

	@Override
	public ApiResponse<RolePermissionsResponse> getPermissionsByRole(UUID roleId) {

		Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found!"));

		List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleId);

		if (rolePermissions.isEmpty()) {
			throw new ResourceNotFoundException("No permissions found for this role");
		}

		Set<UUID> permissionIds = new HashSet<>();
		for (RolePermission rp : rolePermissions) {
			permissionIds.add(rp.getPermissionId());
		}

		List<Permission> permissions = permissionRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(permissionIds);

		Map<UUID, Permission> permissionMap = new HashMap<>();
		for (Permission permission : permissions) {
			permissionMap.put(permission.getId(), permission);
		}

		List<AssignedPermissionWithRolePermissionResponse> permissionResponses = new ArrayList<>();
		for (RolePermission rp : rolePermissions) {

			Permission permission = permissionMap.get(rp.getPermissionId());
			if (permission == null)
				continue; 

			AssignedPermissionWithRolePermissionResponse response = new AssignedPermissionWithRolePermissionResponse();

			response.setPermissionId(permission.getId());
			response.setPermissionCode(permission.getPermissionCode());
			response.setPermissionName(permission.getPermissionName());
			response.setPermissionDescription(permission.getDescription());
			response.setModuleName(permission.getModuleName());
			response.setPermissionActive(permission.getActive());

			response.setAllowed(rp.getActive());
			response.setAssignedActive(rp.getActive());
			response.setAssignedAt(rp.getAssignedAt());

			permissionResponses.add(response);
		}

		RolePermissionsResponse groupResponse = new RolePermissionsResponse();
		groupResponse.setRoleId(role.getId());
		groupResponse.setRoleCode(role.getRoleCode());
		groupResponse.setRoleName(role.getRoleName());
		groupResponse.setRoleDescription(role.getDescription());
		groupResponse.setSystemRole(role.getSystemRole());
		groupResponse.setActive(role.getActive());
		groupResponse.setPermissions(permissionResponses);

		return new ApiResponse<>(true, "Role permissions fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), groupResponse);
	}

//	==================================================================================

	@Override
	public ApiResponse<PermissionRolesResponse> getRolePermissionsByPermissionId(UUID permissionId) {

		Permission permission = permissionRepository.findByIdAndDeletedAtIsNullAndActiveTrue(permissionId)
				.orElseThrow(() -> new ResourceNotFoundException("Permission not found for id " + permissionId));

		List<RolePermission> rolePermissions = rolePermissionRepository.findByPermissionId(permissionId);

		if (rolePermissions.isEmpty()) {
			throw new ResourceNotFoundException("No role-permissions found for this permission");
		}

		Set<UUID> roleIds = new HashSet<>();
		for (RolePermission rp : rolePermissions) {
			roleIds.add(rp.getRoleId());
		}

		List<Role> roles = roleRepository.findByIdInAndDeletedAtIsNull(new ArrayList<>(roleIds));

		Map<UUID, Role> roleMap = new HashMap<>();
		for (Role role : roles) {
			roleMap.put(role.getId(), role);
		}

		List<AssignedRoleResponse> roleResponses = new ArrayList<>();
		for (RolePermission rp : rolePermissions) {
			Role role = roleMap.get(rp.getRoleId());
			if (role == null)
				continue; 

			AssignedRoleResponse roleResp = new AssignedRoleResponse();

			roleResp.setRoleId(role.getId());
			roleResp.setRoleCode(role.getRoleCode());
			roleResp.setRoleName(role.getRoleName());
			roleResp.setRoleDescription(role.getDescription());
			roleResp.setSystemRole(role.getSystemRole());
			roleResp.setRoleIsActive(role.getActive());

			roleResp.setAllowed(rp.getActive());
			roleResp.setAssignedActive(rp.getActive());
			roleResp.setAssignedAt(rp.getAssignedAt());

			roleResponses.add(roleResp);
		}

		PermissionRolesResponse groupResponse = new PermissionRolesResponse();
		groupResponse.setPermissionId(permission.getId());
		groupResponse.setPermissionCode(permission.getPermissionCode());
		groupResponse.setPermissionName(permission.getPermissionName());
		groupResponse.setPermissionDescription(permission.getDescription());
		groupResponse.setModuleName(permission.getModuleName());
		groupResponse.setActive(permission.getActive());
		groupResponse.setRoles(roleResponses);

		return new ApiResponse<>(true, "Permission roles fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), groupResponse);
	}

	@Override
	public ApiResponse<List<RolePermissionsResponse>> getAllRolePermissions() {

		List<Role> roles = roleRepository.findByDeletedAtIsNullAndActiveTrue();
		if (roles.isEmpty()) {
			throw new ResourceNotFoundException("Roles not found");
		}

		List<UUID> roleIds = new ArrayList<>();
		for (Role role : roles) {
			roleIds.add(role.getId());
		}

		List<RolePermission> rolePermissions = rolePermissionRepository
				.findByRoleIdInAndActiveTrueAndDeletedAtIsNull(roleIds);

		List<UUID> permissionIds = new ArrayList<>();
		for (RolePermission rp : rolePermissions) {
			if (!permissionIds.contains(rp.getPermissionId())) {
				permissionIds.add(rp.getPermissionId());
			}
		}

		List<Permission> permissions = permissionRepository.findByIdInAndActiveTrueAndDeletedAtIsNull(permissionIds);

		Map<UUID, Permission> permissionMap = new HashMap<>();
		for (Permission permission : permissions) {
			permissionMap.put(permission.getId(), permission);
		}

		List<RolePermissionsResponse> response = new ArrayList<>();

		for (Role role : roles) {
			RolePermissionsResponse rpgr = new RolePermissionsResponse();
			rpgr.setRoleId(role.getId());
			rpgr.setRoleCode(role.getRoleCode());
			rpgr.setRoleName(role.getRoleName());
			rpgr.setRoleDescription(role.getDescription());
			rpgr.setSystemRole(role.getSystemRole());
			rpgr.setActive(role.getActive());

			List<AssignedPermissionWithRolePermissionResponse> rpList = new ArrayList<>();
			for (RolePermission rp : rolePermissions) {

				if (!rp.getRoleId().equals(role.getId())) {
					continue;
				}

				Permission permission = permissionMap.get(rp.getPermissionId());
				if (permission == null) {
					continue;
				}

				AssignedPermissionWithRolePermissionResponse rpr = new AssignedPermissionWithRolePermissionResponse();
				rpr.setPermissionId(permission.getId());
				rpr.setPermissionCode(permission.getPermissionCode());
				rpr.setPermissionName(permission.getPermissionName());
				rpr.setPermissionDescription(permission.getDescription());
				rpr.setModuleName(permission.getModuleName());
				rpr.setPermissionActive(permission.getActive());

				rpr.setAllowed(rp.getAllowed()); // role-permissions default allowed true
				rpr.setAssignedActive(rp.getActive());
				rpr.setAssignedAt(rp.getAssignedAt());

				rpList.add(rpr);
			}

			rpgr.setPermissions(rpList);
			response.add(rpgr);
		}

		return new ApiResponse<>(true, "Role permissions fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);

	}

}
