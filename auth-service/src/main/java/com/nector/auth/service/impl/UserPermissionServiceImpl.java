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

import com.nector.auth.dto.request.internal.UserPermissionAssignRequest;
import com.nector.auth.dto.request.internal.UserPermissionRevokeRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.AssignedPermissionWithRolePermissionResponse;
import com.nector.auth.dto.response.internal.AssignedUsersResponse;
import com.nector.auth.dto.response.internal.PermissionUsersResponse;
import com.nector.auth.dto.response.internal.UserPermissionsResponse;
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
	public ApiResponse<UserPermissionsResponse> assignOrUpdate(@Valid UserPermissionAssignRequest request,
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

		UserPermission savedUserPermission = userPermissionRepository.save(userPermission);

		return new ApiResponse<>(true, "Permission assigned to user successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), mapToResponse(savedUserPermission, user, permission));
	}

	@Transactional
	@Override
	public ApiResponse<UserPermissionsResponse> revokeUserPermission(@Valid UserPermissionRevokeRequest request,
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

	private UserPermissionsResponse mapToResponse(UserPermission up, User user, Permission permission) {

//		--------USER--------
		UserPermissionsResponse upgr = new UserPermissionsResponse();
		upgr.setUserId(user.getId());
		upgr.setName(user.getName());
		upgr.setEmail(user.getEmail());
		upgr.setMobileNumber(user.getMobileNumber());
		upgr.setActive(user.getActive());

//		--------PERMISSION--------
		AssignedPermissionWithRolePermissionResponse upr = new AssignedPermissionWithRolePermissionResponse();
		upr.setPermissionId(permission.getId());
		upr.setPermissionCode(permission.getPermissionCode());
		upr.setPermissionName(permission.getPermissionName());
		upr.setPermissionDescription(permission.getDescription());
		upr.setModuleName(permission.getModuleName());
		upr.setPermissionActive(permission.getActive());

		upr.setAllowed(up.getAllowed());
		upr.setAssignedActive(up.getActive());
		upr.setAssignedAt(up.getAssignedAt());

		List<AssignedPermissionWithRolePermissionResponse> uprList = List.of(upr);
		upgr.setPermissions(uprList);

		return upgr;
	}

//	=================================================================================
	@Override
	@Transactional
	public ApiResponse<UserPermissionsResponse> getUserPermissionsByUserId(UUID userId) {

		User user = userRepository.findByIdAndActiveTrueAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found for id " + userId));

		List<UserPermission> userPermissions = userPermissionRepository.findByUserIdAndDeletedAtIsNull(userId);

		List<UserRole> userRoles = userRoleRepository.findByUserIdAndActiveTrueAndDeletedAtIsNull(userId);
		List<UUID> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();

		List<RolePermission> rolePermissions = roleIds.isEmpty() ? List.of()
				: rolePermissionRepository.findByRoleIdInAndActiveTrueAndDeletedAtIsNull(roleIds);

		Set<UUID> permissionIds = new HashSet<>();
		rolePermissions.forEach(rp -> permissionIds.add(rp.getPermissionId()));
		userPermissions.forEach(up -> permissionIds.add(up.getPermissionId()));

		List<Permission> permissions = permissionRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(permissionIds);
		Map<UUID, Permission> permissionMap = new HashMap<>();
		for (Permission permission : permissions) {
			permissionMap.put(permission.getId(), permission);
		}

		Map<UUID, AssignedPermissionWithRolePermissionResponse> uprMap = new HashMap<>();

		for (RolePermission rp : rolePermissions) {
			Permission permission = permissionMap.get(rp.getPermissionId());
			if (permission == null) {
				continue;
			}

			AssignedPermissionWithRolePermissionResponse upr = new AssignedPermissionWithRolePermissionResponse();

			upr.setPermissionId(permission.getId());
			upr.setPermissionCode(permission.getPermissionCode());
			upr.setPermissionName(permission.getPermissionName());
			upr.setPermissionDescription(permission.getDescription());
			upr.setModuleName(permission.getModuleName());
			upr.setPermissionActive(permission.getActive());

			upr.setAllowed(rp.getAllowed()); // ROLE default allow
			upr.setAssignedActive(rp.getActive());
			upr.setAssignedAt(rp.getAssignedAt());

			uprMap.put(permission.getId(), upr);
		}

		for (UserPermission up : userPermissions) {

			Permission permission = permissionMap.get(up.getPermissionId());
			if (permission == null) {
				continue;
			}

			AssignedPermissionWithRolePermissionResponse upr = new AssignedPermissionWithRolePermissionResponse();

			upr.setPermissionId(permission.getId());
			upr.setPermissionCode(permission.getPermissionCode());
			upr.setPermissionName(permission.getPermissionName());
			upr.setPermissionDescription(permission.getDescription());
			upr.setModuleName(permission.getModuleName());
			upr.setPermissionActive(permission.getActive());

			upr.setAllowed(up.getAllowed());
			upr.setAssignedActive(up.getActive());
			upr.setAssignedAt(up.getAssignedAt());

			uprMap.put(permission.getId(), upr);
		}

		UserPermissionsResponse response = new UserPermissionsResponse();
		response.setUserId(user.getId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setMobileNumber(user.getMobileNumber());
		response.setActive(user.getActive());
		response.setPermissions(new ArrayList<>(uprMap.values()));

		return new ApiResponse<>(true, "User Permission fetch successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);

	}

	@Override
	@Transactional
	public ApiResponse<PermissionUsersResponse> getUserPermissionsByPermissionId(UUID permissionId) {

		Permission permission = permissionRepository.findByIdAndDeletedAtIsNullAndActiveTrue(permissionId)
				.orElseThrow(() -> new ResourceNotFoundException("Permission not found for id " + permissionId));

		List<UserPermission> userPermissions = userPermissionRepository
				.findByPermissionIdAndDeletedAtIsNull(permissionId);

		List<RolePermission> rolePermissions = rolePermissionRepository
				.findByPermissionIdAndActiveTrueAndDeletedAtIsNull(permissionId);
		List<UUID> roleIds = rolePermissions.stream().map(RolePermission::getRoleId).toList();

		List<UserRole> userRoles = roleIds.isEmpty() ? List.of()
				: userRoleRepository.findByRoleIdInAndActiveTrueAndDeletedAtIsNull(roleIds);

		Set<UUID> userIds = new HashSet<>();
		userRoles.forEach(ur -> userIds.add(ur.getUserId()));
		userPermissions.forEach(up -> userIds.add(up.getUserId()));

		if (userIds.isEmpty()) {
			throw new ResourceNotFoundException("No users found for permission id " + permissionId);
		}

		Map<UUID, User> userMap = userRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(userIds).stream()
				.collect(Collectors.toMap(User::getId, u -> u));

		Map<UUID, AssignedUsersResponse> purMap = new HashMap<>();

		for (UserRole ur : userRoles) {

			User user = userMap.get(ur.getUserId());
			if (user == null)
				continue;

			AssignedUsersResponse pur = new AssignedUsersResponse();

			pur.setUserId(user.getId());
			pur.setName(user.getName());
			pur.setEmail(user.getEmail());
			pur.setMobileNumber(user.getMobileNumber());
			pur.setUserIsActive(user.getActive());

			pur.setAllowed(true); 
			pur.setAssignedActive(ur.getActive());
			pur.setAssignedAt(ur.getAssignedAt());

			purMap.put(user.getId(), pur);
		}

		for (UserPermission up : userPermissions) {

			User user = userMap.get(up.getUserId());
			if (user == null)
				continue;

			AssignedUsersResponse pur = new AssignedUsersResponse();

			pur.setUserId(user.getId());
			pur.setName(user.getName());
			pur.setEmail(user.getEmail());
			pur.setMobileNumber(user.getMobileNumber());
			pur.setUserIsActive(user.getActive());
			
			pur.setAllowed(up.getAllowed());
			pur.setAssignedActive(up.getActive());
			pur.setAssignedAt(up.getAssignedAt());

			purMap.put(user.getId(), pur);
		}

		PermissionUsersResponse response = new PermissionUsersResponse();

		response.setPermissionId(permission.getId());
		response.setPermissionCode(permission.getPermissionCode());
		response.setPermissionName(permission.getPermissionName());
		response.setPermissionDescription(permission.getDescription());
		response.setModuleName(permission.getModuleName());
		response.setActive(permission.getActive());
		response.setUsers(new ArrayList<>(purMap.values()));

		return new ApiResponse<>(true, "Users fetched successfully for permission", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Override
	@Transactional
	public ApiResponse<List<UserPermissionsResponse>> getAllUserPermissions() {

		List<User> users = userRepository.findByDeletedAtIsNull();
		if (users.isEmpty()) {
			throw new ResourceNotFoundException("No users found");
		}

		List<UserPermission> userPermissions = userPermissionRepository.findByDeletedAtIsNull();

		List<UserRole> userRoles = userRoleRepository.findByUserIdInAndActiveTrueAndDeletedAtIsNull(getUserIds(users));

		List<RolePermission> rolePermissions = rolePermissionRepository
				.findByRoleIdInAndActiveTrueAndDeletedAtIsNull(getRoleIds(userRoles));

		List<Permission> permissions = permissionRepository
				.findByIdInAndDeletedAtIsNull(getPermissionIds(rolePermissions, userPermissions));

		Map<UUID, Permission> permissionMap = new HashMap<UUID, Permission>();
		for (Permission p : permissions) {
			permissionMap.put(p.getId(), p);
		}


		Map<UUID, Map<UUID, AssignedPermissionWithRolePermissionResponse>> userPermissionMap = new HashMap<UUID, Map<UUID, AssignedPermissionWithRolePermissionResponse>>();


		for (UserRole ur : userRoles) {

			User user = findUserById(users, ur.getUserId());
			if (user == null)
				continue;

			Map<UUID, AssignedPermissionWithRolePermissionResponse> permissionResponseMap = userPermissionMap.get(user.getId());

			if (permissionResponseMap == null) {
				permissionResponseMap = new HashMap<UUID, AssignedPermissionWithRolePermissionResponse>();
				userPermissionMap.put(user.getId(), permissionResponseMap);
			}

			for (RolePermission rp : rolePermissions) {

				if (!rp.getRoleId().equals(ur.getRoleId()))
					continue;

				Permission perm = permissionMap.get(rp.getPermissionId());
				if (perm == null)
					continue;

				// avoid duplicate
				if (permissionResponseMap.containsKey(perm.getId()))
					continue;

				AssignedPermissionWithRolePermissionResponse upr = new AssignedPermissionWithRolePermissionResponse();
				upr.setPermissionId(perm.getId());
				upr.setPermissionCode(perm.getPermissionCode());
				upr.setPermissionName(perm.getPermissionName());
				upr.setPermissionDescription(perm.getDescription());
				upr.setModuleName(perm.getModuleName());
				upr.setPermissionActive(perm.getActive());

				upr.setAllowed(rp.getAllowed());
				upr.setAssignedActive(rp.getActive());
				upr.setAssignedAt(rp.getAssignedAt());

				permissionResponseMap.put(perm.getId(), upr);
			}
		}

		// ======================================================
		// 5. USER SPECIFIC OVERRIDE (ROLE PERMISSION REPLACED)
		// ======================================================
		for (UserPermission up : userPermissions) {

			User user = findUserById(users, up.getUserId());
			if (user == null)
				continue;

			Permission perm = permissionMap.get(up.getPermissionId());
			if (perm == null)
				continue;

			Map<UUID, AssignedPermissionWithRolePermissionResponse> permissionResponseMap = userPermissionMap.get(user.getId());

			if (permissionResponseMap == null) {
				permissionResponseMap = new HashMap<UUID, AssignedPermissionWithRolePermissionResponse>();
				userPermissionMap.put(user.getId(), permissionResponseMap);
			}

			AssignedPermissionWithRolePermissionResponse upr = new AssignedPermissionWithRolePermissionResponse();
			upr.setPermissionId(perm.getId());
			upr.setPermissionCode(perm.getPermissionCode());
			upr.setPermissionName(perm.getPermissionName());
			upr.setPermissionDescription(perm.getDescription());
			upr.setModuleName(perm.getModuleName());
			upr.setPermissionActive(perm.getActive());

			upr.setAllowed(up.getAllowed());
			upr.setAssignedActive(up.getActive());
			upr.setAssignedAt(up.getAssignedAt());

			// override happens here
			permissionResponseMap.put(perm.getId(), upr);
		}

		// ======================================================
		// 6. GROUP BY USER (FINAL RESPONSE)
		// ======================================================
		List<UserPermissionsResponse> response = new ArrayList<UserPermissionsResponse>();

		for (UUID userId : userPermissionMap.keySet()) {

			User user = findUserById(users, userId);
			if (user == null)
				continue;

			UserPermissionsResponse ugr = new UserPermissionsResponse();

			ugr.setUserId(user.getId());
			ugr.setName(user.getName());
			ugr.setEmail(user.getEmail());
			ugr.setMobileNumber(user.getMobileNumber());
			ugr.setActive(user.getActive());

			List<AssignedPermissionWithRolePermissionResponse> permissionList = new ArrayList<AssignedPermissionWithRolePermissionResponse>(
					userPermissionMap.get(userId).values());

			ugr.setPermissions(permissionList);
			response.add(ugr);
		}

		// ======================================================
		// 7. RETURN
		// ======================================================
		return new ApiResponse<List<UserPermissionsResponse>>(true, "User Permission fetch successfully",
				HttpStatus.OK.name(), HttpStatus.OK.value(), response);
	}

	private List<UUID> getUserIds(List<User> users) {
		List<UUID> ids = new ArrayList<UUID>();
		for (User u : users)
			ids.add(u.getId());
		return ids;
	}

	private List<UUID> getRoleIds(List<UserRole> userRoles) {
		List<UUID> ids = new ArrayList<UUID>();
		for (UserRole ur : userRoles)
			ids.add(ur.getRoleId());
		return ids;
	}

	private List<UUID> getPermissionIds(List<RolePermission> rolePermissions, List<UserPermission> userPermissions) {
		List<UUID> ids = new ArrayList<UUID>();
		for (RolePermission rp : rolePermissions) {
			if (!ids.contains(rp.getPermissionId()))
				ids.add(rp.getPermissionId());
		}
		for (UserPermission up : userPermissions) {
			if (!ids.contains(up.getPermissionId()))
				ids.add(up.getPermissionId());
		}
		return ids;
	}

	private User findUserById(List<User> users, UUID id) {
		for (User u : users) {
			if (u.getId().equals(id))
				return u;
		}
		return null;
	}

}
