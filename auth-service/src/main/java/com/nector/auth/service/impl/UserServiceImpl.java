package com.nector.auth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nector.auth.dto.request.internal.UserCreateRequest;
import com.nector.auth.dto.request.internal.UserUpdateRequest;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.UserResponse;
import com.nector.auth.entity.User;
import com.nector.auth.entity.UserRole;
import com.nector.auth.exception.DuplicateResourceException;
import com.nector.auth.exception.ResourceNotFoundException;
import com.nector.auth.repository.UserRepository;
import com.nector.auth.repository.UserRoleRepository;
import com.nector.auth.service.UserService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final PasswordEncoder passwordEncoder;

	private UUID getLoggedInUserId(Authentication auth) {
		String email = auth.getName(); // JWT sub = email
		User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return user.getId();
	}

	@Transactional
	@Override
	public ApiResponse<?> createUser(UserCreateRequest request, Authentication authentication) {
		
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new DuplicateResourceException("Email is already exists!");
		}

		// Create User
		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setPasswordAlgorithm("BCRYPT");
		user.setMobileNumber(request.getMobileNumber());
		user.setCreatedBy(getLoggedInUserId(authentication));
		User savedUser = userRepository.save(user);

		return new ApiResponse<>(true, "User register successfully...", HttpStatus.CREATED.name(), HttpStatus.CREATED.value(),
				toResponse(savedUser));

	}

	@Transactional
	@Override
	public ApiResponse<UserResponse> updateUser(UUID userId, @Valid UserUpdateRequest request,
			Authentication authentication) {

		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// Update fields
		if (request.getName() != null) {
			user.setName(request.getName().trim());
		}
		if (request.getMobileNumber() != null) {
			user.setMobileNumber(request.getMobileNumber().trim());
		}
		if (request.getPassword() != null) {
			user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		}
		if (request.getActive() != null) {
			user.setActive(request.getActive());
		}

		user.setUpdatedBy(getLoggedInUserId(authentication));
		User updatedUser = userRepository.save(user);

		return new ApiResponse<>(true, "User updated successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), toResponse(updatedUser));
	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> deleteUser(UUID userId, Authentication authentication) {

	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

	    if (user.getDeletedAt() != null) {
	        throw new ResourceNotFoundException("User already deleted!");
	    }

	    UUID loggedInUserId = getLoggedInUserId(authentication);

	    // 1️⃣ Soft delete user
	    user.setDeletedAt(LocalDateTime.now());
	    user.setDeletedBy(loggedInUserId);
	    user.setActive(false);
	    user.setUpdatedBy(loggedInUserId);
	    userRepository.save(user);

	    // 2️⃣ Deactivate all roles assigned to this user
	    List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
	    if (!userRoles.isEmpty()) {
	        for (UserRole userRole : userRoles) {
	            if (Boolean.TRUE.equals(userRole.getActive())) {
	                userRole.setActive(false);
	                userRole.setRevokedAt(LocalDateTime.now());
	                userRole.setRevokedBy(loggedInUserId);
	            }
	            userRole.setDeletedAt(LocalDateTime.now());
	            userRole.setDeletedBy(loggedInUserId);
	            userRoleRepository.save(userRole);
	        }
	        return new ApiResponse<>(true, "User and all assigned roles deleted successfully...",
	        		HttpStatus.OK.name(), HttpStatus.OK.value(), Collections.emptyList());
	    }
	    return new ApiResponse<>(true, "User deleted successfully...",
	    		HttpStatus.OK.name(), HttpStatus.OK.value(), Collections.emptyList());

	}

	@Transactional
	@Override
	public ApiResponse<List<UserResponse>> getAllUsers(Authentication authentication) {
		
		List<User> users = userRepository.findByDeletedAtNull();
		if (users.isEmpty()) {
	        throw new ResourceNotFoundException("Users is not exists!");
		}
		return new ApiResponse<>(true, "Users fetched successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), toResponseList(users));
	}

//	---------------------------------------------------------------
	
	private UserResponse toResponse(User user)  {
		UserResponse res = new UserResponse();
		res.setId(user.getId());
		res.setName(user.getName());
		res.setEmail(user.getEmail());
		res.setMobileNumber(user.getMobileNumber());
		res.setIsActive(user.getActive());
		return res;
	}

	
	private List<UserResponse> toResponseList(List<User> users) {
		List<UserResponse> response = new ArrayList<>();
		
		for (User user : users) {
			UserResponse res = new UserResponse();
			res.setId(user.getId());
			res.setName(user.getName());
			res.setEmail(user.getEmail());
			res.setMobileNumber(user.getMobileNumber());
			res.setIsActive(user.getActive());
			response.add(res);
		}
		return response;
	}
}
