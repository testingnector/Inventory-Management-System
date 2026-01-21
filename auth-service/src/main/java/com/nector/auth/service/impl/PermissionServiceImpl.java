package com.nector.auth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.nector.auth.dto.request.PermissionCreateRequest;
import com.nector.auth.dto.request.PermissionUpdateRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.PermissionResponses;
import com.nector.auth.entity.Permission;
import com.nector.auth.entity.User;
import com.nector.auth.repository.PermissionRepository;
import com.nector.auth.repository.UserRepository;
import com.nector.auth.service.PermissionService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService{
	
	private final PermissionRepository permissionRepository;
	
	private final UserRepository userRepository;
	
	private UUID getLoggedInUserId(Authentication auth) {
		String email = auth.getName(); // JWT sub = email
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found"));
		return user.getId();
	}
	
	@Transactional
	@Override
	public ApiResponse<PermissionResponses> createPermission(@Valid PermissionCreateRequest request,
			Authentication authentication) {
		
		if (permissionRepository.existsByPermissionCode(request.getPermissionCode())) {
            throw new RuntimeException("Permission already exists");
        }

        Permission permission = Permission.builder()
                .permissionCode(request.getPermissionCode())
                .permissionName(request.getPermissionName())
                .description(request.getDescription())
                .moduleName(request.getModuleName())
                .createdAt(LocalDateTime.now())
                .createdBy(getLoggedInUserId(authentication))
                .build();
        
        Permission savedPermission = permissionRepository.save(permission);
        
        return new ApiResponse<>(true, "Permission created successfully...", HttpStatus.CREATED.name(), HttpStatus.CREATED.value(), toResponse(savedPermission));

	}
	

	@Transactional
	@Override
	public ApiResponse<PermissionResponses> updatePermission(UUID permissionId, @Valid PermissionUpdateRequest request,
			Authentication authentication) {

		Permission permission = permissionRepository.findById(permissionId)
	            .orElseThrow(() -> new RuntimeException("Permission not found"));

	    if (request.getPermissionName() != null) {
	        permission.setPermissionName(request.getPermissionName());
	    }

	    if (request.getDescription() != null) {
	        permission.setDescription(request.getDescription());
	    }

	    if (request.getActive() != null) {
	        permission.setActive(request.getActive());
	    }
	    
	    permission.setUpdatedAt(LocalDateTime.now());
	    permission.setUpdatedBy(getLoggedInUserId(authentication));
	    
	    Permission updatedPermission = permissionRepository.save(permission);
	    return new ApiResponse<>(true, "Permission updated successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), toResponse(updatedPermission));

	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> deletePermission(UUID permissionId, Authentication authentication) {
		
		Permission permission = permissionRepository.findById(permissionId)
				.orElseThrow(() -> new RuntimeException("Permission not found"));
		
		if (permission.getDeletedAt() == null) {
			permission.setDeletedAt(LocalDateTime.now());
			permission.setDeletedBy(getLoggedInUserId(authentication));
			permission.setActive(false);
			permissionRepository.save(permission);
			return new ApiResponse<>(true, "Permission deleted successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), Collections.emptyList());
		}
		else {
			return new ApiResponse<>(true, "Permission is already deleted! Why you are trying again and again 😠", HttpStatus.OK.name(), HttpStatus.OK.value(), Collections.emptyList());
		}
		
	}
	
	@Override
	public ApiResponse<List<PermissionResponses>> fetchAllPermission() {
		
		List<Permission> permissions = permissionRepository.findByDeletedAtIsNull();
		return new ApiResponse<>(true, "Permissions fetch successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), toResponseList(permissions));
		
	}

	@Override
	public ApiResponse<PermissionResponses> getSinglePermission(UUID permissionId) {
		
		Permission permission = permissionRepository.findById(permissionId)
				.orElseThrow(() -> new RuntimeException("Permission not found"));
		
		if (permission.getDeletedAt() != null) {
			throw new RuntimeException("Permission is not exists! or already deleted");
		}

		return new ApiResponse<>(true, "Permission fetch successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), toResponse(permission));
	}

//	------------------------------------------------------------------------
	public PermissionResponses toResponse(Permission permission) {
		return PermissionResponses.builder()
				.id(permission.getId())
				.permissionCode(permission.getPermissionCode())
				.permissionName(permission.getPermissionName())
				.description(permission.getDescription())
				.moduleName(permission.getModuleName())
				.isActive(permission.getActive())
				.createdAt(permission.getCreatedAt())
				.build();
	}


	public List<PermissionResponses> toResponseList(List<Permission> permissions) {
		
		List<PermissionResponses> response = new ArrayList<>();
		
		for (Permission permission : permissions) {
			PermissionResponses permissionResponses = PermissionResponses.builder()
					.id(permission.getId())
					.permissionCode(permission.getPermissionCode())
					.permissionName(permission.getPermissionName())
					.description(permission.getDescription())
					.moduleName(permission.getModuleName())
					.isActive(permission.getActive())
					.createdAt(permission.getCreatedAt())
					.build();
			
			response.add(permissionResponses);
		}
		return response;
	}
}
