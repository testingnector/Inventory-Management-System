package com.nector.auth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.auth.client.OrgServiceClient;
import com.nector.auth.dto.request.AssignRoleRequest;
import com.nector.auth.dto.request.RoleRevokeRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.UserRoleResponse;
import com.nector.auth.entity.Role;
import com.nector.auth.entity.User;
import com.nector.auth.entity.UserRole;
import com.nector.auth.entity.UserRoleAudit;
import com.nector.auth.exception.DuplicateResourceException;
import com.nector.auth.exception.InactiveResourceException;
import com.nector.auth.exception.OrgServiceException;
import com.nector.auth.exception.ResourceNotFoundException;
import com.nector.auth.repository.RoleRepository;
import com.nector.auth.repository.UserRepository;
import com.nector.auth.repository.UserRoleAuditRepository;
import com.nector.auth.repository.UserRoleRepository;
import com.nector.auth.service.UserRoleService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserRoleRepository userRoleRepository;
	private final UserRoleAuditRepository userRoleAuditRepository;
	private final OrgServiceClient orgServiceClient;

	private UUID getLoggedInUserId(Authentication auth) {
		String email = auth.getName();
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return user.getId();
	}

	@Transactional
	@Override
	public ApiResponse<UserRoleResponse> assignRole(AssignRoleRequest request, Authentication authentication) {

		UUID loggedInUserId = getLoggedInUserId(authentication);

		String creatorRole = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
				.orElseThrow(() -> new AccessDeniedException("No role found"));

		User user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (!user.getActive()) {
			throw new InactiveResourceException("User is inactive");
		}

		Role role = roleRepository.findById(request.getRoleId())
				.orElseThrow(() -> new ResourceNotFoundException("Role not found"));

		if (!role.getActive()) {
			throw new InactiveResourceException("Role is inactive");
		}

		String roleCode = role.getRoleCode();

		if ("ADMIN".equals(roleCode) && !creatorRole.equals("ROLE_SUPER_ADMIN")) {
			throw new AccessDeniedException("Only SUPER_ADMIN can assign ADMIN role");
		}

		if ("USER".equals(roleCode) && !(creatorRole.equals("ROLE_SUPER_ADMIN") || creatorRole.equals("ROLE_ADMIN"))) {
			throw new AccessDeniedException("Only ADMIN or SUPER_ADMIN can assign USER role");
		}

		if ("SUPER_ADMIN".equals(roleCode) && !creatorRole.equals("ROLE_SUPER_ADMIN")) {
			throw new AccessDeniedException("Only SUPER_ADMIN can assign SUPER_ADMIN role");
		}

		boolean exists = userRoleRepository.existsByUserIdAndRoleIdAndCompanyIdAndActiveTrue(request.getUserId(),
				request.getRoleId(), request.getCompanyId());

		if (exists) {
			throw new DuplicateResourceException("User already has this role for the specified company");
		}

		UserRole revokedRole = userRoleRepository.findByUserIdAndRoleIdAndCompanyIdAndActiveFalse(request.getUserId(),
				request.getRoleId(), request.getCompanyId()).orElse(null);

		UserRole userRole;

		if (revokedRole != null) {

			Boolean oldActive = revokedRole.getActive();

			revokedRole.setActive(true);
			revokedRole.setAssignedAt(LocalDateTime.now());
			revokedRole.setAssignedBy(loggedInUserId);
			revokedRole.setRevokedAt(null);
			revokedRole.setRevokedBy(null);

			userRole = userRoleRepository.save(revokedRole);

			saveAudit(userRole, "REACTIVATE", loggedInUserId, oldActive, true);

		} else {

			userRole = new UserRole();
			userRole.setUserId(request.getUserId());
			userRole.setRoleId(request.getRoleId());

			try {
				orgServiceClient.existsByCompanyId(request.getCompanyId());
			} catch (FeignException e) {

				HttpStatus status = HttpStatus.resolve(e.status());
				String message = (status == HttpStatus.NOT_FOUND)
						? "Company not found with id " + request.getCompanyId()
						: "Error while communicating with Organization Service";

				throw new OrgServiceException(message, status, e);
			}

			userRole.setCompanyId(request.getCompanyId());
			userRole.setBranchId(request.getBranchId());
			userRole.setAssignedBy(loggedInUserId);
			userRole.setAssignedAt(LocalDateTime.now());
			userRole.setCreatedBy(loggedInUserId);
			userRole.setActive(true);

			userRole = userRoleRepository.save(userRole);

			saveAudit(userRole, "ASSIGN", loggedInUserId, false, true);
		}

		return new ApiResponse<>(true, "Role assigned successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				toResponse(userRole, user, role));
	}

	@Transactional
	@Override
	public ApiResponse<UserRoleResponse> revokeRole(RoleRevokeRequest request, Authentication authentication) {

		UUID loggedInUserId = getLoggedInUserId(authentication);

		boolean isSuperAdmin = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

		boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		UserRole userRole = userRoleRepository.findByIdAndActiveTrue(request.getUserRoleId())
				.orElseThrow(() -> new ResourceNotFoundException("Active user role not found"));

		if (userRole.getUserId().equals(loggedInUserId)) {
			throw new AccessDeniedException("You cannot revoke your own role");
		}

		Role role = roleRepository.findById(userRole.getRoleId())
				.orElseThrow(() -> new ResourceNotFoundException("Role not found"));

		User user = userRepository.findById(userRole.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (!user.getActive()) {
			throw new InactiveResourceException("User is inactive");
		}

		if ("SUPER_ADMIN".equals(role.getRoleCode()) && !isSuperAdmin) {
			throw new AccessDeniedException("Only SUPER_ADMIN can revoke SUPER_ADMIN role");
		}

		if ("ADMIN".equals(role.getRoleCode()) && !isSuperAdmin) {
			throw new AccessDeniedException("Only SUPER_ADMIN can revoke ADMIN role");
		}

		Boolean oldActive = userRole.getActive();
		userRole.setActive(false);
		userRole.setRevokedAt(LocalDateTime.now());
		userRole.setRevokedBy(loggedInUserId);

		UserRole revokedUserRole = userRoleRepository.save(userRole);

		saveAudit(revokedUserRole, "REVOKE", loggedInUserId, oldActive, false);

		return new ApiResponse<>(true, "Role revoked successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				toResponse(revokedUserRole, user, role));
	}

	
	
	private void saveAudit(UserRole userRole, String action, UUID performedBy, Boolean oldActive, Boolean newActive) {
		UserRoleAudit audit = new UserRoleAudit();
		audit.setUserRoleId(userRole.getId());
		audit.setUserId(userRole.getUserId());
		audit.setRoleId(userRole.getRoleId());
		audit.setCompanyId(userRole.getCompanyId());
		audit.setBranchId(userRole.getBranchId());
		audit.setAction(action);
		audit.setPerformedBy(performedBy);
		audit.setPerformedAt(LocalDateTime.now());
		audit.setOldIsActive(oldActive);
		audit.setNewIsActive(newActive);

		userRoleAuditRepository.save(audit);
	}

	@Override
	public ApiResponse<List<UserRoleResponse>> getAllUsersRoles() {

		List<UserRole> usersRoles = userRoleRepository.findByDeletedAtNull();
		if (usersRoles.isEmpty()) {
			throw new ResourceNotFoundException("Users Roles is not exists!");
		}

		List<UserRoleResponse> userRolesDetails = new ArrayList<>();

		for (UserRole userRole : usersRoles) {
			Optional<User> userOpt = userRepository.findById(userRole.getUserId());
			Optional<Role> roleOpt = roleRepository.findById(userRole.getRoleId());
			UserRoleResponse userRoleResponse = toResponseList(userRole, userOpt.get(), roleOpt.get());
			userRolesDetails.add(userRoleResponse);
		}

		return new ApiResponse<>(true, "Users Roles fetched successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), userRolesDetails);
	}
//	-------------------------------------------------

	public UserRoleResponse toResponse(UserRole userRole, User user, Role role) {

		UserRoleResponse userRoleResponse = new UserRoleResponse();

		userRoleResponse.setId(userRole.getId());
		userRoleResponse.setUserId(userRole.getUserId());
		userRoleResponse.setName(user.getName());
		userRoleResponse.setEmail(user.getEmail());
		userRoleResponse.setMobileNumber(user.getMobileNumber());
		userRoleResponse.setRoleId(userRole.getRoleId());
		userRoleResponse.setRoleCode(role.getRoleCode());
		userRoleResponse.setRoleName(role.getRoleName());
		userRoleResponse.setActive(userRole.getActive());

		return userRoleResponse;
	}

	public UserRoleResponse toResponseList(UserRole userRole, User user, Role role) {

		UserRoleResponse userRoleResponse = new UserRoleResponse();

		userRoleResponse.setId(userRole.getId());
		userRoleResponse.setUserId(userRole.getUserId());
		userRoleResponse.setName(user.getName());
		userRoleResponse.setEmail(user.getEmail());
		userRoleResponse.setMobileNumber(user.getMobileNumber());
		userRoleResponse.setRoleId(userRole.getRoleId());
		userRoleResponse.setRoleCode(role.getRoleCode());
		userRoleResponse.setRoleName(role.getRoleName());
		userRoleResponse.setActive(userRole.getActive());

		return userRoleResponse;
	}

}
