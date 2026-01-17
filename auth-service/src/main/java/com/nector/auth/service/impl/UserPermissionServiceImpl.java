package com.nector.auth.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.nector.auth.dto.request.UserPermissionRequest;
import com.nector.auth.dto.request.UserPermissionRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.UserPermissionResponse;
import com.nector.auth.entity.Permission;
import com.nector.auth.entity.User;
import com.nector.auth.entity.UserPermission;
import com.nector.auth.exception.ResourceNotFoundException;
import com.nector.auth.repository.PermissionRepository;
import com.nector.auth.repository.UserPermissionRepository;
import com.nector.auth.repository.UserRepository;
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

	private UUID getLoggedInUserId(Authentication auth) {
		String email = auth.getName(); // JWT sub = email
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return user.getId();
	}

	@Override
	public ApiResponse<UserPermissionResponse> assignOrUpdate(@Valid UserPermissionRequest request,
			Authentication authentication) {

		User user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User is not found"));

		Permission permission = permissionRepository.findById(request.getPermissionId())
				.orElseThrow(() -> new ResourceNotFoundException("Permission is not found"));

		UserPermission userPermission = userPermissionRepository
				.findByUserIdAndPermissionIdAndActiveTrue(request.getUserId(), request.getPermissionId())
				.orElseGet(() -> {
					UserPermission up = new UserPermission();
					up.setUserId(request.getUserId());
					up.setPermissionId(request.getPermissionId());
					up.setCreatedBy(getLoggedInUserId(authentication));
					return up;
				});

		userPermission.setAllowed(request.getAllowed());
		userPermission.setActive(true);
		userPermission.setAssignedAt(LocalDateTime.now());
		userPermission.setAssignedBy(getLoggedInUserId(authentication));
		userPermission.setRevokedAt(null);
		userPermission.setRevokedBy(null);

		if (userPermission.getId() != null) {
			userPermission.setUpdatedBy(getLoggedInUserId(authentication));
		}

		UserPermission savedUserPermission = userPermissionRepository.save(userPermission);

		return new ApiResponse<>(true, "Permission assigned to user successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), mapToResponse(savedUserPermission));
	}

	@Override
	public ApiResponse<UserPermissionResponse> revokeUserPermission(@Valid UserPermissionRevokeRequest request,
			Authentication authentication) {

		UserPermission userPermission = userPermissionRepository
				.findByUserIdAndPermissionId(request.getUserId(), request.getPermissionId())
				.orElseThrow(() -> new ResourceNotFoundException("User Permission not found"));

		userPermission.setActive(false);
		userPermission.setRevokedAt(LocalDateTime.now());
		userPermission.setRevokedBy(getLoggedInUserId(authentication));

		UserPermission revokedUserPermissioin = userPermissionRepository.save(userPermission);

		return new ApiResponse<>(true, "Permission revoked to user successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), mapToResponse(revokedUserPermissioin));
	}


	private UserPermissionResponse mapToResponse(UserPermission userPermission) {

		UserPermissionResponse upr = new UserPermissionResponse();
		upr.setId(userPermission.getId());
		upr.setUserId(userPermission.getUserId());
		upr.setPermissionId(userPermission.getPermissionId());
		upr.setAllowed(userPermission.getAllowed());
		upr.setActive(userPermission.getActive());
		upr.setAssignedAt(userPermission.getAssignedAt());

		return upr;
	}

//	=================================================================================
	@Override
	@Transactional
	public ApiResponse<UserPermissionResponse> getPermissionsByUserId(UUID userId) {
		
		UserPermission userPermission = userPermissionRepository.findByUserIdAndActiveTrue(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User Permission not found"));
		
		// user details , permission details
		
		return new ApiResponse<>(true, "User Permission fetch successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), mapToResponse(userPermission));
	}
}
