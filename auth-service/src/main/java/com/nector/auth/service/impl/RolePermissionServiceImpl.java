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

import com.nector.auth.dto.request.RolePermissionAssignRequest;
import com.nector.auth.dto.request.RolePermissionRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.role_permission.PermissionRoleGroupResponse;
import com.nector.auth.dto.response.role_permission.PermissionRoleResponse;
import com.nector.auth.dto.response.role_permission.RolePermissionGroupResponse;
import com.nector.auth.dto.response.role_permission.RolePermissionResponse;
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
	public ApiResponse<RolePermissionGroupResponse> assignPermission(RolePermissionAssignRequest request,
			Authentication authentication) {

		UUID assignedBy = getLoggedInUserId(authentication);

		// 1️⃣ Fetch role
		Role role = roleRepository.findById(request.getRoleId())
				.orElseThrow(() -> new ResourceNotFoundException("Role not found"));

		// 2️⃣ Fetch existing RolePermission mappings in one query
		List<RolePermission> existingRolePermissions = rolePermissionRepository
				.findByRoleIdAndPermissionIdIn(request.getRoleId(), request.getPermissionIds());

		Map<UUID, RolePermission> existingMap = existingRolePermissions.stream()
				.collect(Collectors.toMap(RolePermission::getPermissionId, rp -> rp));

		List<RolePermission> rolePermissionsToSave = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();

		// 3️⃣ Loop over requested permissionIds
		for (UUID permissionId : request.getPermissionIds()) {
			RolePermission rp = existingMap.get(permissionId);

			if (rp == null) {
				// New assignment
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

		// 4️⃣ Save all RolePermission entities in batch
		rolePermissionRepository.saveAll(rolePermissionsToSave);

		// 5️⃣ Fetch permission details in one query
		List<Permission> permissions = permissionRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(
				request.getPermissionIds().stream().collect(Collectors.toSet()));

		// 6️⃣ Build RolePermissionResponse list
		List<RolePermissionResponse> permissionResponses = new ArrayList<>();
		for (Permission p : permissions) {

			RolePermission rp = existingMap.get(p.getId());
			if (rp == null) {
				// maybe just assigned now
				for (RolePermission r : rolePermissionsToSave) {
					if (r.getPermissionId().equals(p.getId())) {
						rp = r;
						break;
					}
				}
			}

			RolePermissionResponse rpr = new RolePermissionResponse();
			rpr.setPermissionId(p.getId());
			rpr.setPermissionCode(p.getPermissionCode());
			rpr.setPermissionName(p.getPermissionName());
			rpr.setPermissionDescription(p.getDescription());
			rpr.setModuleName(p.getModuleName());
			rpr.setPermissionIsActive(p.getActive());

			rpr.setAllowed(rp != null && rp.getActive());
			rpr.setAssignedActive(rp != null ? rp.getActive() : false);
			rpr.setAssignedAt(rp != null ? rp.getAssignedAt() : null);

			permissionResponses.add(rpr);
		}

		// 7️⃣ Build final RolePermissionGroupResponse
		RolePermissionGroupResponse response = new RolePermissionGroupResponse();
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
	public ApiResponse<RolePermissionGroupResponse> revokePermission(RolePermissionRevokeRequest request,
			Authentication authentication) {

		// 1️⃣ Logged-in user
		UUID revokedBy = getLoggedInUserId(authentication);

		// 2️⃣ Fetch RolePermission mapping
		RolePermission rolePermission = rolePermissionRepository
				.findByRoleIdAndPermissionId(request.getRoleId(), request.getPermissionId())
				.orElseThrow(() -> new ResourceNotFoundException("Role-Permission mapping not found"));

		// 3️⃣ Check if already revoked
		if (!rolePermission.getActive()) {
			throw new RuntimeException("Permission already revoked");
		}

		// 4️⃣ Revoke permission
		rolePermission.setActive(false);
		rolePermission.setRevokedAt(LocalDateTime.now());
		rolePermission.setRevokedBy(revokedBy);
		rolePermissionRepository.save(rolePermission);

		// 5️⃣ Fetch all RolePermissions for this role
		List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(request.getRoleId());
		if (rolePermissions.isEmpty()) {
			throw new ResourceNotFoundException("No permissions found for this role");
		}

		// 6️⃣ Fetch role info
		Role role = roleRepository.findById(request.getRoleId())
				.orElseThrow(() -> new ResourceNotFoundException("Role not found!"));

		// 7️⃣ Collect permissionIds
		List<UUID> permissionIds = new ArrayList<>();
		for (RolePermission rp : rolePermissions) {
			permissionIds.add(rp.getPermissionId());
		}

		// 8️⃣ Fetch permissions in one go
		List<Permission> permissions = permissionRepository.findAllById(permissionIds);
		Map<UUID, Permission> permissionMap = new HashMap<>();
		for (Permission p : permissions) {
			permissionMap.put(p.getId(), p);
		}

		// 9️⃣ Build RolePermissionResponse list
		List<RolePermissionResponse> permissionResponses = new ArrayList<>();
		for (RolePermission rp : rolePermissions) {
			Permission permission = permissionMap.get(rp.getPermissionId());
			if (permission == null)
				continue;

			RolePermissionResponse rpr = new RolePermissionResponse();
			rpr.setPermissionId(permission.getId());
			rpr.setPermissionCode(permission.getPermissionCode());
			rpr.setPermissionName(permission.getPermissionName());
			rpr.setPermissionDescription(permission.getDescription());
			rpr.setModuleName(permission.getModuleName());
			rpr.setPermissionIsActive(permission.getActive());

			rpr.setAllowed(rp.getActive());
			rpr.setAssignedActive(rp.getActive());
			rpr.setAssignedAt(rp.getAssignedAt());

			permissionResponses.add(rpr);
		}

		// 🔟 Build final RolePermissionGroupResponse
		RolePermissionGroupResponse response = new RolePermissionGroupResponse();
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
	public ApiResponse<RolePermissionGroupResponse> getPermissionsByRole(UUID roleId) {

		// 1️⃣ Fetch role info
		Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found!"));

		// 2️⃣ Fetch all role-permissions for this role
		List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleId);

		if (rolePermissions.isEmpty()) {
			throw new ResourceNotFoundException("No permissions found for this role");
		}

		// 3️⃣ Collect permissionIds
		Set<UUID> permissionIds = new HashSet<>();
		for (RolePermission rp : rolePermissions) {
			permissionIds.add(rp.getPermissionId());
		}

		// 4️⃣ Fetch all permissions in one query
		List<Permission> permissions = permissionRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(permissionIds);

		Map<UUID, Permission> permissionMap = new HashMap<>();
		for (Permission permission : permissions) {
			permissionMap.put(permission.getId(), permission);
		}

		// 5️⃣ Build permission response list
		List<RolePermissionResponse> permissionResponses = new ArrayList<>();
		for (RolePermission rp : rolePermissions) {

			Permission permission = permissionMap.get(rp.getPermissionId());
			if (permission == null)
				continue; // skip if permission deleted or inactive

			RolePermissionResponse response = new RolePermissionResponse();

			// Permission info
			response.setPermissionId(permission.getId());
			response.setPermissionCode(permission.getPermissionCode());
			response.setPermissionName(permission.getPermissionName());
			response.setPermissionDescription(permission.getDescription());
			response.setModuleName(permission.getModuleName());
			response.setPermissionIsActive(permission.getActive());

			// Role-Permission mapping info
			response.setAllowed(rp.getActive());
			response.setAssignedActive(rp.getActive());
			response.setAssignedAt(rp.getAssignedAt());

			permissionResponses.add(response);
		}

		// 6️⃣ Build final group response
		RolePermissionGroupResponse groupResponse = new RolePermissionGroupResponse();
		groupResponse.setRoleId(role.getId());
		groupResponse.setRoleCode(role.getRoleCode());
		groupResponse.setRoleName(role.getRoleName());
		groupResponse.setRoleDescription(role.getDescription());
		groupResponse.setSystemRole(role.getSystemRole());
		groupResponse.setActive(role.getActive());
		groupResponse.setPermissions(permissionResponses);

		// 7️⃣ Return wrapped API response
		return new ApiResponse<>(true, "Role permissions fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), groupResponse);
	}

//	==================================================================================

	@Override
	public ApiResponse<PermissionRoleGroupResponse> getRolePermissionsByPermissionId(UUID permissionId) {

		// 1️⃣ Fetch permission
		Permission permission = permissionRepository.findByIdAndDeletedAtIsNullAndActiveTrue(permissionId)
				.orElseThrow(() -> new ResourceNotFoundException("Permission not found for id " + permissionId));

		// 2️⃣ Fetch all RolePermission mappings for this permission
		List<RolePermission> rolePermissions = rolePermissionRepository.findByPermissionId(permissionId);

		if (rolePermissions.isEmpty()) {
			throw new ResourceNotFoundException("No role-permissions found for this permission");
		}

		// 3️⃣ Collect all roleIds
		Set<UUID> roleIds = new HashSet<>();
		for (RolePermission rp : rolePermissions) {
			roleIds.add(rp.getRoleId());
		}

		// 4️⃣ Fetch roles in one query
		List<Role> roles = roleRepository.findByIdInAndDeletedAtIsNull(new ArrayList<>(roleIds));

		Map<UUID, Role> roleMap = new HashMap<>();
		for (Role role : roles) {
			roleMap.put(role.getId(), role);
		}

		// 5️⃣ Build role responses
		List<PermissionRoleResponse> roleResponses = new ArrayList<>();
		for (RolePermission rp : rolePermissions) {
			Role role = roleMap.get(rp.getRoleId());
			if (role == null)
				continue; // safety check if role deleted

			PermissionRoleResponse roleResp = new PermissionRoleResponse();

			// Role info
			roleResp.setRoleId(role.getId());
			roleResp.setRoleCode(role.getRoleCode());
			roleResp.setRoleName(role.getRoleName());
			roleResp.setRoleDescription(role.getDescription());
			roleResp.setSystemRole(role.getSystemRole());
			roleResp.setRoleIsActive(role.getActive());

			// Mapping info
			roleResp.setAllowed(rp.getActive());
			roleResp.setAssignedActive(rp.getActive());
			roleResp.setAssignedAt(rp.getAssignedAt());

			roleResponses.add(roleResp);
		}

		// 6️⃣ Build final group response
		PermissionRoleGroupResponse groupResponse = new PermissionRoleGroupResponse();
		groupResponse.setPermissionId(permission.getId());
		groupResponse.setPermissionCode(permission.getPermissionCode());
		groupResponse.setPermissionName(permission.getPermissionName());
		groupResponse.setPermissionDescription(permission.getDescription());
		groupResponse.setModuleName(permission.getModuleName());
		groupResponse.setActive(permission.getActive());
		groupResponse.setRoles(roleResponses);

		// 7️⃣ Return API response
		return new ApiResponse<>(true, "Permission roles fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), groupResponse);
	}

	@Override
	public ApiResponse<List<RolePermissionGroupResponse>> getAllRolePermissions() {

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

		List<RolePermissionGroupResponse> response = new ArrayList<>();

		for (Role role : roles) {
			RolePermissionGroupResponse rpgr = new RolePermissionGroupResponse();
			rpgr.setRoleId(role.getId());
			rpgr.setRoleCode(role.getRoleCode());
			rpgr.setRoleName(role.getRoleName());
			rpgr.setRoleDescription(role.getDescription());
			rpgr.setSystemRole(role.getSystemRole());
			rpgr.setActive(role.getActive());

			List<RolePermissionResponse> rpList = new ArrayList<>();
			for (RolePermission rp : rolePermissions) {

				if (!rp.getRoleId().equals(role.getId())) {
					continue;
				}

				Permission permission = permissionMap.get(rp.getPermissionId());
				if (permission == null) {
					continue;
				}

				RolePermissionResponse rpr = new RolePermissionResponse();
				rpr.setPermissionId(permission.getId());
				rpr.setPermissionCode(permission.getPermissionCode());
				rpr.setPermissionName(permission.getPermissionName());
				rpr.setPermissionDescription(permission.getDescription());
				rpr.setModuleName(permission.getModuleName());
				rpr.setPermissionIsActive(permission.getActive());

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
