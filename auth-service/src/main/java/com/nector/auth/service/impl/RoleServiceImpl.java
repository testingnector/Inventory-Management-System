package com.nector.auth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.auth.dto.request.RoleCreateRequest;
import com.nector.auth.dto.request.RoleUpdateRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.RoleResponse;
import com.nector.auth.entity.Role;
import com.nector.auth.entity.User;
import com.nector.auth.exception.DuplicateResourceException;
import com.nector.auth.exception.ResourceNotFoundException;
import com.nector.auth.repository.RoleRepository;
import com.nector.auth.repository.UserRepository;
import com.nector.auth.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

	private final RoleRepository roleRepository;
	
	private final UserRepository userRepository;

	@Transactional
	@Override
	public ApiResponse<RoleResponse> createRole(RoleCreateRequest request, Authentication authentication) {

		if (roleRepository.existsByRoleCode(request.getRoleCode())) {
			throw new DuplicateResourceException("Role already exists");
		}

		Role role = new Role();
		role.setRoleCode(request.getRoleCode());
		role.setRoleName(request.getRoleName());
		role.setDescription(request.getDescription());
		role.setSystemRole(request.getSystemRole());
		role.setCreatedBy(getLoggedInUserId(authentication));

		Role savedRole = roleRepository.save(role);

		return new ApiResponse<>(true, "Role create successfully...", HttpStatus.CREATED.name(), HttpStatus.CREATED.value(), mapToResponse(savedRole));
	}

	@Transactional
	@Override
	public ApiResponse<RoleResponse> updateRole(UUID roleId, RoleUpdateRequest roleUpdateRequest, Authentication authentication) {

		Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found"));

		// ❌ System role protection
		if (Boolean.TRUE.equals(role.getSystemRole())) {
			throw new AccessDeniedException("System roles cannot be modified");
		}

		if (roleUpdateRequest.getRoleName() != null) {
			role.setRoleName(roleUpdateRequest.getRoleName());			
		}
		if (roleUpdateRequest.getDescription() != null) {
			role.setDescription(roleUpdateRequest.getDescription());
		}
		if (roleUpdateRequest.getActive() != null) {
			role.setActive(roleUpdateRequest.getActive());
		}
		
		role.setUpdatedBy(getLoggedInUserId(authentication));
		
		Role updatedRole = roleRepository.save(role);
		
		return new ApiResponse<>(true, "Role update successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), mapToResponse(updatedRole));
	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> deleteRole(UUID roleId, Authentication authentication) {

		Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found"));

		// System role protection
		if (Boolean.TRUE.equals(role.getSystemRole())) {
			throw new AccessDeniedException("System roles cannot be deleted");
		}

		if (role.getDeletedAt() != null) {
			return new ApiResponse<>(true, "Role is not exists! or already deleted...", HttpStatus.NOT_FOUND.name(), HttpStatus.NOT_FOUND.value(), Collections.emptyList());
		}
		role.setActive(false);
		role.setDeletedAt(LocalDateTime.now());
		role.setDeletedBy(getLoggedInUserId(authentication));
		role.setUpdatedBy(getLoggedInUserId(authentication));
		roleRepository.save(role);
		return new ApiResponse<>(true, "Role deleted successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), Collections.emptyList());
	}

	@Override
	public ApiResponse<List<RoleResponse>> getAllRoles() {
		List<Role> roles = roleRepository.findByDeletedAtIsNull();
		return new ApiResponse<>(true, "Roles fetch successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), mapToResponseList(roles));
	}

	private UUID getLoggedInUserId(Authentication auth) {
	    String email = auth.getName(); // JWT sub = email
	    User user = userRepository.findByEmail(email)
	                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
	    return user.getId();
	}


	private RoleResponse mapToResponse(Role role) {
		RoleResponse res = new RoleResponse();
		res.setId(role.getId());
		res.setRoleCode(role.getRoleCode());
		res.setRoleName(role.getRoleName());
		res.setRoleDescription(role.getDescription());
		res.setIsSystemRole(role.getSystemRole());
		res.setIsActive(role.getActive());
		return res;
	}
	
	private List<RoleResponse> mapToResponseList(List<Role> roles) {
		
		List<RoleResponse> roleResponseList = new ArrayList<>();
		
		for (Role role : roles) {			

			RoleResponse res = new RoleResponse();
			res.setId(role.getId());
			res.setRoleCode(role.getRoleCode());
			res.setRoleName(role.getRoleName());
			res.setRoleDescription(role.getDescription());
			res.setIsSystemRole(role.getSystemRole());
			res.setIsActive(role.getActive());
			roleResponseList.add(res);
			
		}
		return roleResponseList;
	}

}
