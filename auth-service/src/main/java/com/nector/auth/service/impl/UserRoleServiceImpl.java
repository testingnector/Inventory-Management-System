package com.nector.auth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.auth.client.OrgServiceClient;
import com.nector.auth.dto.request.external.CompanyIdsRequestDto;
import com.nector.auth.dto.request.internal.UserRoleAssignRequest;
import com.nector.auth.dto.request.internal.UserRoleRevokeRequest;
import com.nector.auth.dto.response.external.CompanyResponseExternalDto;
import com.nector.auth.dto.response.external.CompanyUsersResponseExternalDto;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.dto.response.internal.AssignedUserAndUserRoleResponse;
import com.nector.auth.dto.response.internal.CompanyResponseInternalDto;
import com.nector.auth.dto.response.internal.CompanyRolesResponse;
import com.nector.auth.dto.response.internal.CompanyUsersResponse;
import com.nector.auth.dto.response.internal.RoleAndUserRoleResponse;
import com.nector.auth.dto.response.internal.RoleCompaniesResponse;
import com.nector.auth.dto.response.internal.UserCompaniesResponse;
import com.nector.auth.dto.response.internal.UserCompanyResponse;
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
	public ApiResponse<UserCompaniesResponse> assignRole(UserRoleAssignRequest request,
			Authentication authentication) {

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

		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();

		if (revokedRole != null) {

			Boolean oldActive = revokedRole.getActive();

			revokedRole.setActive(true);
			revokedRole.setAssignedAt(LocalDateTime.now());
			revokedRole.setAssignedBy(loggedInUserId);
			revokedRole.setRevokedAt(null);
			revokedRole.setRevokedBy(null);

			try {
				CompanyResponseExternalDto companyResponseExternalDto = orgServiceClient
						.getCompanyBasic(request.getCompanyId()).getBody().getData();
				
				crid.setCompanyId(companyResponseExternalDto.getCompanyId());
				crid.setCompanyCode(companyResponseExternalDto.getCompanyCode());
				crid.setCompanyName(companyResponseExternalDto.getCompanyName());
				crid.setActive(companyResponseExternalDto.getActive());

			} catch (FeignException e) {
				HttpStatus status = HttpStatus.resolve(e.status());
				String message = (status == HttpStatus.INTERNAL_SERVER_ERROR) ? "Something went wrong!"
						: "Error while communicating with Organization Service";

				throw new OrgServiceException(message, status, e);
			}

			userRole = userRoleRepository.save(revokedRole);

			saveAudit(userRole, "REACTIVATE", loggedInUserId, oldActive, true);

		} else {

			userRole = new UserRole();
			userRole.setUserId(request.getUserId());
			userRole.setRoleId(request.getRoleId());

			try {
				CompanyResponseExternalDto companyResponseExternalDto = orgServiceClient
						.getCompanyBasic(request.getCompanyId()).getBody().getData();
				
				crid.setCompanyId(companyResponseExternalDto.getCompanyId());
				crid.setCompanyCode(companyResponseExternalDto.getCompanyCode());
				crid.setCompanyName(companyResponseExternalDto.getCompanyName());
				crid.setActive(companyResponseExternalDto.getActive());

			} catch (FeignException e) {
				HttpStatus status = HttpStatus.resolve(e.status());
				String message = (status == HttpStatus.INTERNAL_SERVER_ERROR) ? "Something went wrong!"
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

		// BUILD RESPONSE

		UserCompaniesResponse userResp = new UserCompaniesResponse();
		userResp.setUserId(user.getId());
		userResp.setName(user.getName());
		userResp.setEmail(user.getEmail());
		userResp.setMobileNumber(user.getMobileNumber());
		userResp.setActive(user.getActive());

		// -------- COMPANY --------
		CompanyRolesResponse companyResp = new CompanyRolesResponse();
		companyResp.setCompanyId(crid.getCompanyId());
		companyResp.setCompanyCode(crid.getCompanyCode());
		companyResp.setCompanyName(crid.getCompanyName());
		companyResp.setActive(crid.getActive());

		// -------- ROLE --------
		RoleAndUserRoleResponse roleResp = new RoleAndUserRoleResponse();
		roleResp.setRoleId(role.getId());
		roleResp.setRoleCode(role.getRoleCode());
		roleResp.setRoleName(role.getRoleName());
		roleResp.setActive(role.getActive());
		roleResp.setUserRoleId(userRole.getId());
		roleResp.setAssignedIsActive(userRole.getActive());
		roleResp.setAssignedAt(userRole.getAssignedAt());
		roleResp.setRevokedAt(userRole.getRevokedAt());

		// role → company
		companyResp.getRoles().add(roleResp);

		// company → user
		userResp.getCompanies().add(companyResp);

		// wrap in response
		return new ApiResponse<UserCompaniesResponse>(true, "Role assigned successfully",
				HttpStatus.OK.name(), HttpStatus.OK.value(), userResp);

	}

	@Transactional
	@Override
	public ApiResponse<UserCompaniesResponse> revokeRole(UserRoleRevokeRequest request,
			Authentication authentication) {

		UUID loggedInUserId = getLoggedInUserId(authentication);

		boolean isSuperAdmin = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

		boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		UserRole userRole = userRoleRepository.findByIdAndActiveTrueAndDeletedAtIsNull(request.getUserRoleId())
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

		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();
		try {
			CompanyResponseExternalDto companyResponseExternalDto = orgServiceClient
					.getCompanyBasic(userRole.getCompanyId()).getBody().getData();
			
			crid.setCompanyId(companyResponseExternalDto.getCompanyId());
			crid.setCompanyCode(companyResponseExternalDto.getCompanyCode());
			crid.setCompanyName(companyResponseExternalDto.getCompanyName());
			crid.setActive(companyResponseExternalDto.getActive());

		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.INTERNAL_SERVER_ERROR) ? "Something went wrong!"
					: "Error while communicating with Organization Service";

			throw new OrgServiceException(message, status, e);
		}

		// BUILD RESPONSE

		UserCompaniesResponse userResp = new UserCompaniesResponse();
		userResp.setUserId(user.getId());
		userResp.setName(user.getName());
		userResp.setEmail(user.getEmail());
		userResp.setMobileNumber(user.getMobileNumber());
		userResp.setActive(user.getActive());

		// -------- COMPANY --------
		CompanyRolesResponse companyResp = new CompanyRolesResponse();
		companyResp.setCompanyId(crid.getCompanyId());
		companyResp.setCompanyCode(crid.getCompanyCode());
		companyResp.setCompanyName(crid.getCompanyName());
		companyResp.setActive(crid.getActive());

		// -------- ROLE --------
		RoleAndUserRoleResponse roleResp = new RoleAndUserRoleResponse();
		roleResp.setRoleId(role.getId());
		roleResp.setRoleCode(role.getRoleCode());
		roleResp.setRoleName(role.getRoleName());
		roleResp.setActive(role.getActive());
		roleResp.setUserRoleId(revokedUserRole.getId());
		roleResp.setAssignedIsActive(revokedUserRole.getActive()); // revoked
		roleResp.setAssignedAt(revokedUserRole.getAssignedAt());
		roleResp.setRevokedAt(revokedUserRole.getRevokedAt());

		companyResp.getRoles().add(roleResp);
		userResp.getCompanies().add(companyResp);

		saveAudit(revokedUserRole, "REVOKE", loggedInUserId, oldActive, false);

		return new ApiResponse<UserCompaniesResponse>(true, "Role revoked successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), userResp);

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

	@Transactional
	@Override
	public ApiResponse<List<UserCompaniesResponse>> getAllUsersRoles() {

		List<User> users = userRepository.findByDeletedAtIsNull();
		if (users == null) {
			users = new ArrayList<User>();
		}

		List<UUID> userIds = new ArrayList<UUID>();
		Map<UUID, User> userMap = new HashMap<UUID, User>();
		for (User user : users) {
			userIds.add(user.getId());
			userMap.put(user.getId(), user);
		}

		List<UserRole> usersRoles = userRoleRepository.findByUserIdInAndDeletedAtNull(userIds);
		if (usersRoles == null) {
			usersRoles = new ArrayList<UserRole>();
		}

		List<UUID> companyIds = new ArrayList<UUID>();
		List<UUID> roleIds = new ArrayList<UUID>();
		for (UserRole userRole : usersRoles) {
			if (userRole.getCompanyId() != null)
				companyIds.add(userRole.getCompanyId());
			if (userRole.getRoleId() != null)
				roleIds.add(userRole.getRoleId());
		}

		List<CompanyResponseExternalDto> companyResponseExternalDtos = new ArrayList<CompanyResponseExternalDto>();
		if (!companyIds.isEmpty()) {
			try {
				CompanyIdsRequestDto cird = new CompanyIdsRequestDto();
				cird.setCompanyIds(companyIds);
				List<CompanyResponseExternalDto> temp = orgServiceClient.getCompaniesDetailsByCompanyIds(cird).getBody().getData();
				if (temp != null) {
					companyResponseExternalDtos = temp;
				}
			} catch (FeignException e) {
				HttpStatus status = HttpStatus.resolve(e.status());
				String message = (status == HttpStatus.INTERNAL_SERVER_ERROR) ? "Something went wrong!"
						: "Error while communicating with Organization Service";

				throw new OrgServiceException(message, status, e);
			}
		}

		Map<UUID, CompanyResponseExternalDto> companyMap = new HashMap<UUID, CompanyResponseExternalDto>();
		for (CompanyResponseExternalDto cred : companyResponseExternalDtos) {
			companyMap.put(cred.getCompanyId(), cred);
		}

		List<Role> roles = new ArrayList<Role>();
		if (!roleIds.isEmpty()) {
			List<Role> tempRoles = roleRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(roleIds);
			if (tempRoles != null) {
				roles = tempRoles;
			}
		}
		Map<UUID, Role> roleMap = new HashMap<UUID, Role>();
		for (Role role : roles) {
			roleMap.put(role.getId(), role);
		}

		// BUILD RESPONSE
		Map<UUID, UserCompaniesResponse> userResponseMap = new HashMap<UUID, UserCompaniesResponse>();

		// First, create entries for all users (even if no roles)
		for (User user : users) {
			UserCompaniesResponse userResp = new UserCompaniesResponse();
			userResp.setUserId(user.getId());
			userResp.setName(user.getName());
			userResp.setEmail(user.getEmail());
			userResp.setMobileNumber(user.getMobileNumber());
			userResp.setActive(user.getActive());
			userResponseMap.put(user.getId(), userResp);
		}

		// Then, attach companies and roles from usersRoles
		for (UserRole userRole : usersRoles) {
			UserCompaniesResponse userResp = userResponseMap.get(userRole.getUserId());
			if (userResp == null) {
				continue;
			}

			CompanyResponseExternalDto company = companyMap.get(userRole.getCompanyId());
			Role role = roleMap.get(userRole.getRoleId());

			if (company == null || role == null) {
				continue;
			}

			// COMPANY LEVEL
			CompanyRolesResponse companyResp = null;
			for (CompanyRolesResponse c : userResp.getCompanies()) {
				if (c.getCompanyId().equals(company.getCompanyId())) {
					companyResp = c;
					break;
				}
			}

			if (companyResp == null) {
				companyResp = new CompanyRolesResponse();
				companyResp.setCompanyId(company.getCompanyId());
				companyResp.setCompanyCode(company.getCompanyCode());
				companyResp.setCompanyName(company.getCompanyName());
				companyResp.setActive(company.getActive());
				userResp.getCompanies().add(companyResp);
			}

			// ROLE LEVEL
			RoleAndUserRoleResponse roleResp = new RoleAndUserRoleResponse();
			roleResp.setRoleId(role.getId());
			roleResp.setRoleCode(role.getRoleCode());
			roleResp.setRoleName(role.getRoleName());
			roleResp.setActive(role.getActive());
			roleResp.setUserRoleId(userRole.getId());
			roleResp.setAssignedIsActive(userRole.getActive());
			roleResp.setAssignedAt(userRole.getAssignedAt());
			roleResp.setRevokedAt(userRole.getRevokedAt());

			companyResp.getRoles().add(roleResp);
		}

		List<UserCompaniesResponse> finalResponse = new ArrayList<UserCompaniesResponse>();
		for (UserCompaniesResponse u : userResponseMap.values()) {
			finalResponse.add(u);
		}

		return new ApiResponse<List<UserCompaniesResponse>>(true, "Users roles fetched successfully",
				HttpStatus.OK.name(), HttpStatus.OK.value(), finalResponse);
	}

//	-------------------------------------------------

	@Transactional
	@Override
	public ApiResponse<RoleCompaniesResponse> getUserRolesByRoleId(UUID roleId) {

		Role role = roleRepository.findByIdAndDeletedAtIsNullAndActiveTrue(roleId)
				.orElseThrow(() -> new ResourceNotFoundException("Role not found!"));

		List<UserRole> userRoles = userRoleRepository.findByRoleIdAndDeletedAtIsNull(roleId);

		RoleCompaniesResponse response = new RoleCompaniesResponse();
		response.setRoleId(role.getId());
		response.setRoleCode(role.getRoleCode());
		response.setRoleName(role.getRoleName());
		response.setActive(role.getActive());

		if (userRoles.isEmpty()) {
			return new ApiResponse<>(true, "No users assigned to this role", HttpStatus.OK.name(),
					HttpStatus.OK.value(), response);
		}

		List<UUID> userIds = new ArrayList<>();
		List<UUID> companyIds = new ArrayList<>();

		for (UserRole ur : userRoles) {
			userIds.add(ur.getUserId());
			companyIds.add(ur.getCompanyId());
		}

		// -------- USERS --------
		List<User> users = userRepository.findByIdInAndDeletedAtIsNull(userIds);
		Map<UUID, User> userMap = new HashMap<>();
		for (User u : users) {
			userMap.put(u.getId(), u);
		}

		// -------- COMPANIES --------
		CompanyIdsRequestDto cir = new CompanyIdsRequestDto();
		cir.setCompanyIds(companyIds);

		List<CompanyResponseExternalDto> companies;
		try {
			companies = orgServiceClient.getCompaniesDetailsByCompanyIds(cir).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			throw new OrgServiceException("Error while communicating with Organization Service", status, e);
		}

		Map<UUID, CompanyResponseExternalDto> companyMap = new HashMap<>();
		for (CompanyResponseExternalDto c : companies) {
			companyMap.put(c.getCompanyId(), c);
		}

		Map<UUID, CompanyUsersResponse> companyResponseMap = new HashMap<>();

		for (UserRole ur : userRoles) {

			User user = userMap.get(ur.getUserId());
			CompanyResponseExternalDto company = companyMap.get(ur.getCompanyId());

			if (user == null || company == null) {
				continue;
			}

			CompanyUsersResponse companyResp = companyResponseMap.get(company.getCompanyId());
			if (companyResp == null) {
				companyResp = new CompanyUsersResponse();
				companyResp.setCompanyId(company.getCompanyId());
				companyResp.setCompanyCode(company.getCompanyCode());
				companyResp.setCompanyName(company.getCompanyName());
				companyResp.setActive(company.getActive());
				companyResponseMap.put(company.getCompanyId(), companyResp);
			}

			AssignedUserAndUserRoleResponse userResp = new AssignedUserAndUserRoleResponse();
			userResp.setUserId(user.getId());
			userResp.setName(user.getName());
			userResp.setEmail(user.getEmail());
			userResp.setMobileNumber(user.getMobileNumber());
			userResp.setUserIsActive(user.getActive());
			userResp.setUserRoleId(ur.getId());
			userResp.setAssignedIsActive(ur.getActive());
			userResp.setAssignedAt(ur.getAssignedAt());
			userResp.setRevokedAt(ur.getRevokedAt());

			companyResp.getUsers().add(userResp);
		}

		response.getCompanies().addAll(companyResponseMap.values());

		return new ApiResponse<>(true, "Assigned users fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Transactional
	@Override
	public ApiResponse<UserCompaniesResponse> getUserRolesByUserId(UUID userId) {

		User user = userRepository.findByIdAndActiveTrueAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		UserCompaniesResponse response = new UserCompaniesResponse();
		response.setUserId(user.getId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setMobileNumber(user.getMobileNumber());
		response.setActive(user.getActive());

		List<UserRole> userRoles = userRoleRepository.findByUserIdAndDeletedAtIsNull(userId);
		if (userRoles.isEmpty()) {
			return new ApiResponse<>(true, "User has no roles", HttpStatus.OK.name(), HttpStatus.OK.value(), response);
		}

		List<UUID> roleIds = new ArrayList<>();
		List<UUID> companyIds = new ArrayList<>();

		for (UserRole ur : userRoles) {
			roleIds.add(ur.getRoleId());
			companyIds.add(ur.getCompanyId());
		}

		List<Role> roles = roleRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(roleIds);
		Map<UUID, Role> roleMap = new HashMap<>();
		for (Role r : roles) {
			roleMap.put(r.getId(), r);
		}

		CompanyIdsRequestDto cir = new CompanyIdsRequestDto();
		cir.setCompanyIds(companyIds);

		List<CompanyResponseExternalDto> companies;
		try {
			companies = orgServiceClient.getCompaniesDetailsByCompanyIds(cir).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			throw new OrgServiceException("Error while communicating with Organization Service", status, e);
		}

		Map<UUID, CompanyRolesResponse> companyMap = new HashMap<>();
		for (CompanyResponseExternalDto c : companies) {
			CompanyRolesResponse crd = new CompanyRolesResponse();
			crd.setCompanyId(c.getCompanyId());
			crd.setCompanyCode(c.getCompanyCode());
			crd.setCompanyName(c.getCompanyName());
			crd.setActive(c.getActive());
			companyMap.put(c.getCompanyId(), crd);
		}

		for (UserRole ur : userRoles) {

			Role role = roleMap.get(ur.getRoleId());
			CompanyRolesResponse company = companyMap.get(ur.getCompanyId());

			if (role == null || company == null) {
				continue;
			}

			RoleAndUserRoleResponse rrd = new RoleAndUserRoleResponse();
			rrd.setRoleId(role.getId());
			rrd.setRoleCode(role.getRoleCode());
			rrd.setRoleName(role.getRoleName());
			rrd.setActive(role.getActive());
			rrd.setUserRoleId(ur.getId());
			rrd.setAssignedIsActive(ur.getActive());
			rrd.setAssignedAt(ur.getAssignedAt());
			rrd.setRevokedAt(ur.getRevokedAt());

			company.getRoles().add(rrd);
		}

		response.getCompanies().addAll(companyMap.values());

		return new ApiResponse<>(true, "User roles fetched successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				response);
	}

	@Transactional
	@Override
	public ApiResponse<UserCompanyResponse> getUserRolesByUserIdAndCompanyId(UUID userId, UUID companyId) {

		User user = userRepository.findByIdAndActiveTrueAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		CompanyResponseExternalDto companyBasic;
		try {
			ResponseEntity<ApiResponse<CompanyResponseExternalDto>> companyResponse = orgServiceClient
					.getCompanyBasic(companyId);
			companyBasic = companyResponse.getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}

		List<UserRole> userRoles = userRoleRepository.findByUserIdAndCompanyIdAndDeletedAtIsNull(userId, companyId);
		List<UUID> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();

		List<Role> roles = roleRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(roleIds);
		Map<UUID, Role> roleMap = roles.stream().collect(Collectors.toMap(Role::getId, r -> r));

		UserCompanyResponse response = new UserCompanyResponse();
		response.setUserId(user.getId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setMobileNumber(user.getMobileNumber());
		response.setActive(user.getActive());

		CompanyRolesResponse companyDto = new CompanyRolesResponse();
		companyDto.setCompanyId(companyBasic.getCompanyId());
		companyDto.setCompanyCode(companyBasic.getCompanyCode());
		companyDto.setCompanyName(companyBasic.getCompanyName());
		companyDto.setActive(companyBasic.getActive());

		for (UserRole ur : userRoles) {
			Role role = roleMap.get(ur.getRoleId());
			if (role == null)
				continue;

			RoleAndUserRoleResponse roleDto = new RoleAndUserRoleResponse();
			roleDto.setRoleId(role.getId());
			roleDto.setRoleCode(role.getRoleCode());
			roleDto.setRoleName(role.getRoleName());
			roleDto.setActive(role.getActive());
			roleDto.setUserRoleId(ur.getId());
			roleDto.setAssignedIsActive(ur.getActive());
			roleDto.setAssignedAt(ur.getCreatedAt());
			roleDto.setRevokedAt(ur.getDeletedAt());

			companyDto.getRoles().add(roleDto);
		}

		response.setCompany(companyDto);

		String message = userRoles.isEmpty() ? "User has no roles for this company" : "User roles fetched successfully";

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), response);
	}

	@Override
	public ApiResponse<List<CompanyUsersResponseExternalDto>> getAllUsersByCompanyId(UUID companyId) {

		List<UserRole> userRoles = userRoleRepository.getActiveUsersWithActiveRolesByCompanyId(companyId);

		Set<UUID> userIds = new HashSet<>();
		for (UserRole userRole : userRoles) {
			userIds.add(userRole.getUserId());
		}
		if (userIds.isEmpty()) {
			return new ApiResponse<>(true, "No users registered for this company", HttpStatus.OK.name(),
					HttpStatus.OK.value(), Collections.emptyList());
		}

		List<User> users = userRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(userIds);
		List<CompanyUsersResponseExternalDto> companyUsersResponseDto1s = new ArrayList<>();
		for (User user : users) {
			CompanyUsersResponseExternalDto curd = new CompanyUsersResponseExternalDto();
			curd.setUserId(user.getId());
			curd.setName(user.getName());
			curd.setEmail(user.getEmail());
			curd.setMobileNumber(user.getMobileNumber());
			curd.setActive(user.getActive());

			companyUsersResponseDto1s.add(curd);
		}

		String message = companyUsersResponseDto1s.isEmpty() ? "No users registered" : "Users fetched successfully";
		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), companyUsersResponseDto1s);
	}

}
