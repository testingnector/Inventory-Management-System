package com.nector.auth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.nector.auth.dto.request.UserPermissionRequest;
import com.nector.auth.dto.request.UserPermissionRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.UserPermissionResponse;
import com.nector.auth.entity.Permission;
import com.nector.auth.entity.RolePermission;
import com.nector.auth.entity.User;
import com.nector.auth.entity.UserPermission;
import com.nector.auth.entity.UserRole;
import com.nector.auth.exception.DuplicateResourceException;
import com.nector.auth.exception.ResourceNotFoundException;
import com.nector.auth.repository.PermissionRepository;
import com.nector.auth.repository.RolePermissionRepository;
import com.nector.auth.repository.UserPermissionRepository;
import com.nector.auth.repository.UserRepository;
import com.nector.auth.repository.UserRoleRepository;
import com.nector.auth.service.UserPermissionService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPermissionServiceImpl implements UserPermissionService {

	private final PermissionRepository permissionRepository;
	private final UserPermissionRepository userPermissionRepository;
	private final UserRepository userRepository;
	private final RolePermissionRepository rolePermissionRepository;
	private final UserRoleRepository userRoleRepository;

	private UUID getLoggedInUserId(Authentication auth) {
		String email = auth.getName(); // JWT sub = email
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return user.getId();
	}

	@Transactional
	@Override
	public ApiResponse<UserPermissionResponse> assignOrUpdate(@Valid UserPermissionRequest request,
			Authentication authentication) {

		User user = userRepository.findByIdAndDeletedAtIsNull(request.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User is not found"));

		Permission permission = permissionRepository.findByIdAndDeletedAtIsNull(request.getPermissionId())
				.orElseThrow(() -> new ResourceNotFoundException("Permission is not found"));

		UserPermission userPermission = userPermissionRepository
				.findByUserIdAndPermissionIdAndDeletedAtIsNull(request.getUserId(), request.getPermissionId())
				.orElse(null);

		if (userPermission != null && Boolean.TRUE.equals(userPermission.getActive())) {
			throw new DuplicateResourceException("Permission already assigned to this user");
		}

		if (userPermission == null) {
			userPermission = new UserPermission();
			userPermission.setUserId(request.getUserId());
			userPermission.setPermissionId(request.getPermissionId());
			userPermission.setCreatedBy(getLoggedInUserId(authentication));
		} else {
			userPermission.setUpdatedBy(getLoggedInUserId(authentication));
		}

		userPermission.setAllowed(request.getAllowed());
		userPermission.setActive(true);
		userPermission.setAssignedAt(LocalDateTime.now());
		userPermission.setAssignedBy(getLoggedInUserId(authentication));
		userPermission.setRevokedAt(null);
		userPermission.setRevokedBy(null);

		UserPermission saved = userPermissionRepository.save(userPermission);

		return new ApiResponse<>(true, "Permission assigned to user successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), mapToResponse(saved, user, permission));
	}

	@Transactional
	@Override
	public ApiResponse<UserPermissionResponse> revokeUserPermission(@Valid UserPermissionRevokeRequest request,
			Authentication authentication) {

		User user = userRepository.findByIdAndDeletedAtIsNull(request.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User is not found"));

		Permission permission = permissionRepository.findByIdAndDeletedAtIsNull(request.getPermissionId())
				.orElseThrow(() -> new ResourceNotFoundException("Permission is not found"));

		UserPermission userPermission = userPermissionRepository
				.findByUserIdAndPermissionIdAndActiveTrueAndDeletedAtIsNull(request.getUserId(),
						request.getPermissionId())
				.orElseThrow(() -> new ResourceNotFoundException("Permission is already revoked or deleted"));

		userPermission.setActive(false);
		userPermission.setRevokedAt(LocalDateTime.now());
		userPermission.setRevokedBy(getLoggedInUserId(authentication));

		UserPermission saved = userPermissionRepository.save(userPermission);

		return new ApiResponse<>(true, "Permission revoked from user successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), mapToResponse(saved, user, permission));
	}

	private UserPermissionResponse mapToResponse(UserPermission up, User user, Permission permission) {

		UserPermissionResponse upr = new UserPermissionResponse();

		upr.setUserId(user.getId());
		upr.setName(user.getName());
		upr.setEmail(user.getEmail());
		upr.setMobileNumber(user.getMobileNumber());

		upr.setPermissionId(permission.getId());
		upr.setPermissionCode(permission.getPermissionCode());
		upr.setPermissionName(permission.getPermissionName());
		upr.setDescription(permission.getDescription());
		upr.setModuleName(permission.getModuleName());

		upr.setAllowed(up.getAllowed());
		upr.setActive(up.getActive());
		upr.setAssignedAt(up.getAssignedAt());

		return upr;
	}

//	=================================================================================
	@Override
	@Transactional
	public ApiResponse<List<UserPermissionResponse>> getUserPermissionsByUserId(UUID userId) {

		// 1️⃣ USER (not deleted)
		User user = userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found for id " + userId));

		// 2️⃣ USER-SPECIFIC permissions (not deleted)
		List<UserPermission> userPermissions = userPermissionRepository.findByUserIdAndDeletedAtIsNull(userId);

		// 3️⃣ USER ROLES (active + not deleted)
		List<UserRole> userRoles = userRoleRepository.findByUserIdAndActiveTrueAndDeletedAtIsNull(userId);
		List<UUID> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();


		// 4️⃣ ROLE PERMISSIONS (active + not deleted)
		List<RolePermission> rolePermissions = roleIds.isEmpty() ? List.of()
				: rolePermissionRepository.findByRoleIdInAndActiveTrueAndDeletedAtIsNull(roleIds);

		// 🔑 permissionId → response
		Map<UUID, UserPermissionResponse> permissionMap = new HashMap<>();

		// 5️⃣ ROLE DEFAULT PERMISSIONS
		for (RolePermission rp : rolePermissions) {

			Permission permission = permissionRepository.findByIdAndDeletedAtIsNull(rp.getPermissionId()).orElse(null);

			if (permission == null)
				continue;

			UserPermissionResponse upr = new UserPermissionResponse();

			upr.setUserId(user.getId());
			upr.setName(user.getName());
			upr.setEmail(user.getEmail());
			upr.setMobileNumber(user.getMobileNumber());

			upr.setPermissionId(permission.getId());
			upr.setPermissionCode(permission.getPermissionCode());
			upr.setPermissionName(permission.getPermissionName());
			upr.setDescription(permission.getDescription());
			upr.setModuleName(permission.getModuleName());

			upr.setAllowed(true); // ROLE = default allow
			upr.setActive(true);
			upr.setAssignedAt(rp.getAssignedAt());

			permissionMap.put(permission.getId(), upr);
		}

		// 6️⃣ USER OVERRIDE (ALLOW / DENY / REVOKE)
		for (UserPermission up : userPermissions) {

			// ⛔ safety
			if (up.getDeletedAt() != null)
				continue;

			Permission permission = permissionRepository.findByIdAndDeletedAtIsNull(up.getPermissionId()).orElse(null);

			if (permission == null)
				continue;

			UserPermissionResponse upr = new UserPermissionResponse();

			upr.setUserId(user.getId());
			upr.setName(user.getName());
			upr.setEmail(user.getEmail());
			upr.setMobileNumber(user.getMobileNumber());

			upr.setPermissionId(permission.getId());
			upr.setPermissionCode(permission.getPermissionCode());
			upr.setPermissionName(permission.getPermissionName());
			upr.setDescription(permission.getDescription());
			upr.setModuleName(permission.getModuleName());

			upr.setAllowed(up.getAllowed()); // 🔥 override allow / deny
			upr.setActive(up.getActive()); // 🔥 revoke respected
			upr.setAssignedAt(up.getAssignedAt());

			// 🔥 USER ALWAYS OVERRIDES ROLE
			permissionMap.put(permission.getId(), upr);
		}

		// 7️⃣ FINAL LIST
		if (permissionMap.isEmpty()) {
			throw new ResourceNotFoundException("No permission found for user");
		}

		return new ApiResponse<>(true, "User Permission fetch successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), new ArrayList<>(permissionMap.values()));
	}

	@Override
	@Transactional
	public ApiResponse<List<UserPermissionResponse>> getUserPermissionsByPermissionId(UUID permissionId) {

		// 1️⃣ Permission must exist & not deleted
		Permission permission = permissionRepository.findByIdAndDeletedAtIsNull(permissionId)
				.orElseThrow(() -> new ResourceNotFoundException("Permission not found for id " + permissionId));

		// 2️⃣ USER-SPECIFIC permissions (not deleted)
		List<UserPermission> userPermissions = userPermissionRepository
				.findByPermissionIdAndDeletedAtIsNull(permissionId);

		// 3️⃣ ROLE IDs having this permission (ACTIVE + NOT DELETED)
		List<RolePermission> rolePermissions = rolePermissionRepository
				.findByPermissionIdAndActiveTrueAndDeletedAtIsNull(permissionId);
		List<UUID> roleIds = rolePermissions.stream().map(RolePermission::getRoleId).toList();

		// 4️⃣ USERS having those roles (ACTIVE + NOT DELETED)
		List<UserRole> userRoles = roleIds.isEmpty() ? List.of()
				: userRoleRepository.findByRoleIdInAndActiveTrueAndDeletedAtIsNull(roleIds);

		// 🔑 userId → response (USER overrides ROLE)
		Map<UUID, UserPermissionResponse> responseMap = new HashMap<>();

		// 5️⃣ ROLE DEFAULT USERS
		for (UserRole ur : userRoles) {

			User user = userRepository.findByIdAndDeletedAtIsNull(ur.getUserId()).orElse(null);

			if (user == null)
				continue;

			UserPermissionResponse upr = new UserPermissionResponse();

			upr.setUserId(user.getId());
			upr.setName(user.getName());
			upr.setEmail(user.getEmail());
			upr.setMobileNumber(user.getMobileNumber());

			upr.setPermissionId(permission.getId());
			upr.setPermissionCode(permission.getPermissionCode());
			upr.setPermissionName(permission.getPermissionName());
			upr.setDescription(permission.getDescription());
			upr.setModuleName(permission.getModuleName());

			upr.setAllowed(true); // ROLE DEFAULT = ALLOW
			upr.setActive(true);
			upr.setAssignedAt(ur.getAssignedAt());

			responseMap.put(user.getId(), upr);
		}

		// 6️⃣ USER OVERRIDE (ALLOW / DENY / REVOKE)
		for (UserPermission up : userPermissions) {

			// ⛔ Skip deleted records (extra safety)
			if (up.getDeletedAt() != null)
				continue;

			User user = userRepository.findByIdAndDeletedAtIsNull(up.getUserId()).orElse(null);

			if (user == null)
				continue;

			UserPermissionResponse upr = new UserPermissionResponse();

			upr.setUserId(user.getId());
			upr.setName(user.getName());
			upr.setEmail(user.getEmail());
			upr.setMobileNumber(user.getMobileNumber());

			upr.setPermissionId(permission.getId());
			upr.setPermissionCode(permission.getPermissionCode());
			upr.setPermissionName(permission.getPermissionName());
			upr.setDescription(permission.getDescription());
			upr.setModuleName(permission.getModuleName());

			upr.setAllowed(up.getAllowed()); // 🔥 override allow / deny
			upr.setActive(up.getActive()); // 🔥 revoke respected
			upr.setAssignedAt(up.getAssignedAt());

			// 🔥 USER ALWAYS OVERRIDES ROLE
			responseMap.put(user.getId(), upr);
		}

		// 7️⃣ Final response
		if (responseMap.isEmpty()) {
			throw new ResourceNotFoundException("No users found for permission id " + permissionId);
		}

		return new ApiResponse<>(true, "User Permission fetch successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), new ArrayList<>(responseMap.values()));
	}

}
