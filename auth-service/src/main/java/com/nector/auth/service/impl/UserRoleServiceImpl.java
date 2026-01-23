package com.nector.auth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.nector.auth.dto.request.UserRoleAssignRequest;
import com.nector.auth.dto.request.UserRoleRevokeRequest;
import com.nector.auth.dto.request.external.CompanyBasicResponse;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.external.CompanyIdsRequest;
import com.nector.auth.dto.response.user_role.CompanyResponseDto;
import com.nector.auth.dto.response.user_role.RoleCompaniesUsersResponseDto1;
import com.nector.auth.dto.response.user_role.RoleCompaniesUsersResponseDto2;
import com.nector.auth.dto.response.user_role.RoleCompaniesUsersResponseDto3;
import com.nector.auth.dto.response.user_role.UserCompaniesRolesResponseDto1;
import com.nector.auth.dto.response.user_role.UserCompaniesRolesResponseDto2;
import com.nector.auth.dto.response.user_role.UserCompaniesRolesResponseDto3;
import com.nector.auth.dto.response.user_role.UserCompanyRolesResponseDto1;
import com.nector.auth.dto.response.user_role.UserCompanyRolesResponseDto2;
import com.nector.auth.dto.response.user_role.UserCompanyRolesResponseDto3;
import com.nector.auth.dto.response.user_role.UsersCompaniesRolesResponseDto1;
import com.nector.auth.dto.response.user_role.UsersCompaniesRolesResponseDto2;
import com.nector.auth.dto.response.user_role.UsersCompaniesRolesResponseDto3;
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
	public ApiResponse<UsersCompaniesRolesResponseDto1> assignRole(UserRoleAssignRequest request, Authentication authentication) {

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

		CompanyResponseDto crd = new CompanyResponseDto();

		if (revokedRole != null) {

			Boolean oldActive = revokedRole.getActive();

			revokedRole.setActive(true);
			revokedRole.setAssignedAt(LocalDateTime.now());
			revokedRole.setAssignedBy(loggedInUserId);
			revokedRole.setRevokedAt(null);
			revokedRole.setRevokedBy(null);

			try {
				ResponseEntity<ApiResponse<CompanyBasicResponse>> companyBasicResponseEntity = orgServiceClient
						.getCompanyBasic(request.getCompanyId());
				CompanyBasicResponse companyBasicResponse = companyBasicResponseEntity.getBody().getData();
				crd.setCompanyId(companyBasicResponse.getCompanyId());
				crd.setCompanyCode(companyBasicResponse.getCompanyCode());
				crd.setCompanyName(companyBasicResponse.getCompanyName());
				crd.setActive(companyBasicResponse.getActive());

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
				ResponseEntity<ApiResponse<CompanyBasicResponse>> companyBasicResponseEntity = orgServiceClient
						.getCompanyBasic(request.getCompanyId());
				CompanyBasicResponse companyBasicResponse = companyBasicResponseEntity.getBody().getData();
				crd.setCompanyId(companyBasicResponse.getCompanyId());
				crd.setCompanyCode(companyBasicResponse.getCompanyCode());
				crd.setCompanyName(companyBasicResponse.getCompanyName());
				crd.setActive(companyBasicResponse.getActive());

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

		UsersCompaniesRolesResponseDto1 userResp = new UsersCompaniesRolesResponseDto1();
		userResp.setUserId(user.getId());
		userResp.setName(user.getName());
		userResp.setEmail(user.getEmail());
		userResp.setMobileNumber(user.getMobileNumber());
		userResp.setActive(user.getActive());

		// -------- COMPANY --------
		UsersCompaniesRolesResponseDto2 companyResp = new UsersCompaniesRolesResponseDto2();
		companyResp.setCompanyId(crd.getCompanyId());
		companyResp.setCompanyCode(crd.getCompanyCode());
		companyResp.setCompanyName(crd.getCompanyName());
		companyResp.setActive(crd.getActive());

		// -------- ROLE --------
		UsersCompaniesRolesResponseDto3 roleResp = new UsersCompaniesRolesResponseDto3();
		roleResp.setRoleId(role.getId());
		roleResp.setRoleCode(role.getRoleCode());
		roleResp.setRoleName(role.getRoleName());
		roleResp.setRoleIsActive(role.getActive());
		roleResp.setUserRoleId(userRole.getId());
		roleResp.setAssignedIsActive(userRole.getActive());
		roleResp.setAssignedAt(userRole.getAssignedAt());
		roleResp.setRevokedAt(userRole.getRevokedAt());

		// role → company
		companyResp.getRoles().add(roleResp);

		// company → user
		userResp.getCompanies().add(companyResp);

		// wrap in response
		return new ApiResponse<UsersCompaniesRolesResponseDto1>(true, "Role assigned successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), userResp);

	}

	@Transactional
	@Override
	public ApiResponse<UsersCompaniesRolesResponseDto1> revokeRole(UserRoleRevokeRequest request, Authentication authentication) {

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

		CompanyResponseDto crd = new CompanyResponseDto();
		try {
			ResponseEntity<ApiResponse<CompanyBasicResponse>> companyBasicResponseEntity = orgServiceClient
					.getCompanyBasic(userRole.getCompanyId());
			CompanyBasicResponse companyBasicResponse = companyBasicResponseEntity.getBody().getData();
			crd.setCompanyId(companyBasicResponse.getCompanyId());
			crd.setCompanyCode(companyBasicResponse.getCompanyCode());
			crd.setCompanyName(companyBasicResponse.getCompanyName());
			crd.setActive(companyBasicResponse.getActive());

		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.INTERNAL_SERVER_ERROR) ? "Something went wrong!"
					: "Error while communicating with Organization Service";

			throw new OrgServiceException(message, status, e);
		}

		// BUILD RESPONSE

		UsersCompaniesRolesResponseDto1 userResp = new UsersCompaniesRolesResponseDto1();
		userResp.setUserId(user.getId());
		userResp.setName(user.getName());
		userResp.setEmail(user.getEmail());
		userResp.setMobileNumber(user.getMobileNumber());
		userResp.setActive(user.getActive());

		// -------- COMPANY --------
		UsersCompaniesRolesResponseDto2 companyResp = new UsersCompaniesRolesResponseDto2();
		companyResp.setCompanyId(crd.getCompanyId());
		companyResp.setCompanyCode(crd.getCompanyCode());
		companyResp.setCompanyName(crd.getCompanyName());
		companyResp.setActive(crd.getActive());

		// -------- ROLE --------
		UsersCompaniesRolesResponseDto3 roleResp = new UsersCompaniesRolesResponseDto3();
		roleResp.setRoleId(role.getId());
		roleResp.setRoleCode(role.getRoleCode());
		roleResp.setRoleName(role.getRoleName());
		roleResp.setRoleIsActive(role.getActive());
		roleResp.setUserRoleId(revokedUserRole.getId());
		roleResp.setAssignedIsActive(revokedUserRole.getActive()); // revoked
		roleResp.setAssignedAt(revokedUserRole.getAssignedAt());
		roleResp.setRevokedAt(revokedUserRole.getRevokedAt());

		companyResp.getRoles().add(roleResp);
		userResp.getCompanies().add(companyResp);

		saveAudit(revokedUserRole, "REVOKE", loggedInUserId, oldActive, false);

		return new ApiResponse<UsersCompaniesRolesResponseDto1>(true, "Role revoked successfully", HttpStatus.OK.name(),
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
	public ApiResponse<List<UsersCompaniesRolesResponseDto1>> getAllUsersRoles() {

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

		List<CompanyBasicResponse> companyBasicResponses = new ArrayList<CompanyBasicResponse>();
		if (!companyIds.isEmpty()) {
			try {
				CompanyIdsRequest cir = new CompanyIdsRequest();
				cir.setCompanyIds(companyIds);
				List<CompanyBasicResponse> temp = orgServiceClient.getCompanyBasicByCompanyIds(cir).getBody().getData();
				if (temp != null) {
					companyBasicResponses = temp;
				}
			} catch (FeignException e) {
				HttpStatus status = HttpStatus.resolve(e.status());
				String message = (status == HttpStatus.INTERNAL_SERVER_ERROR) ? "Something went wrong!"
						: "Error while communicating with Organization Service";

				throw new OrgServiceException(message, status, e);
			}
		}

		Map<UUID, CompanyBasicResponse> companyMap = new HashMap<UUID, CompanyBasicResponse>();
		for (CompanyBasicResponse cbr : companyBasicResponses) {
			companyMap.put(cbr.getCompanyId(), cbr);
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
		Map<UUID, UsersCompaniesRolesResponseDto1> userResponseMap = new HashMap<UUID, UsersCompaniesRolesResponseDto1>();

		// First, create entries for all users (even if no roles)
		for (User user : users) {
			UsersCompaniesRolesResponseDto1 userResp = new UsersCompaniesRolesResponseDto1();
			userResp.setUserId(user.getId());
			userResp.setName(user.getName());
			userResp.setEmail(user.getEmail());
			userResp.setMobileNumber(user.getMobileNumber());
			userResp.setActive(user.getActive());
			userResponseMap.put(user.getId(), userResp);
		}

		// Then, attach companies and roles from usersRoles
		for (UserRole userRole : usersRoles) {
			UsersCompaniesRolesResponseDto1 userResp = userResponseMap.get(userRole.getUserId());
			if (userResp == null) {
				continue;
			}

			CompanyBasicResponse company = companyMap.get(userRole.getCompanyId());
			Role role = roleMap.get(userRole.getRoleId());

			if (company == null || role == null) {
				continue;
			}

			// COMPANY LEVEL
			UsersCompaniesRolesResponseDto2 companyResp = null;
			for (UsersCompaniesRolesResponseDto2 c : userResp.getCompanies()) {
				if (c.getCompanyId().equals(company.getCompanyId())) {
					companyResp = c;
					break;
				}
			}

			if (companyResp == null) {
				companyResp = new UsersCompaniesRolesResponseDto2();
				companyResp.setCompanyId(company.getCompanyId());
				companyResp.setCompanyCode(company.getCompanyCode());
				companyResp.setCompanyName(company.getCompanyName());
				companyResp.setActive(company.getActive());
				userResp.getCompanies().add(companyResp);
			}

			// ROLE LEVEL
			UsersCompaniesRolesResponseDto3 roleResp = new UsersCompaniesRolesResponseDto3();
			roleResp.setRoleId(role.getId());
			roleResp.setRoleCode(role.getRoleCode());
			roleResp.setRoleName(role.getRoleName());
			roleResp.setRoleIsActive(role.getActive());
			roleResp.setUserRoleId(userRole.getId());
			roleResp.setAssignedIsActive(userRole.getActive());
			roleResp.setAssignedAt(userRole.getAssignedAt());
			roleResp.setRevokedAt(userRole.getRevokedAt());

			companyResp.getRoles().add(roleResp);
		}

		List<UsersCompaniesRolesResponseDto1> finalResponse = new ArrayList<UsersCompaniesRolesResponseDto1>();
		for (UsersCompaniesRolesResponseDto1 u : userResponseMap.values()) {
			finalResponse.add(u);
		}

		return new ApiResponse<List<UsersCompaniesRolesResponseDto1>>(true, "Users roles fetched successfully",
				HttpStatus.OK.name(), HttpStatus.OK.value(), finalResponse);
	}

//	-------------------------------------------------

	@Transactional
	@Override
	public ApiResponse<RoleCompaniesUsersResponseDto1> getUserRolesByRoleId(UUID roleId) {

		Role role = roleRepository.findByIdAndDeletedAtIsNullAndActiveTrue(roleId)
				.orElseThrow(() -> new ResourceNotFoundException("Role not found!"));

		List<UserRole> userRoles = userRoleRepository.findByRoleIdAndDeletedAtIsNull(roleId);

		// -------- ROLE LEVEL --------
		RoleCompaniesUsersResponseDto1 response = new RoleCompaniesUsersResponseDto1();
		response.setRoleId(role.getId());
		response.setRoleCode(role.getRoleCode());
		response.setRoleName(role.getRoleName());
		response.setActive(role.getActive());

		if (userRoles.isEmpty()) {
			return new ApiResponse<>(true, "No users assigned to this role", HttpStatus.OK.name(),
					HttpStatus.OK.value(), response);
		}

		// -------- COLLECT IDS --------
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
		CompanyIdsRequest cir = new CompanyIdsRequest();
		cir.setCompanyIds(companyIds);

		List<CompanyBasicResponse> companies;
		try {
			companies = orgServiceClient.getCompanyBasicByCompanyIds(cir).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			throw new OrgServiceException("Error while communicating with Organization Service", status, e);
		}

		Map<UUID, CompanyBasicResponse> companyMap = new HashMap<>();
		for (CompanyBasicResponse c : companies) {
			companyMap.put(c.getCompanyId(), c);
		}

		// -------- BUILD RESPONSE --------
		Map<UUID, RoleCompaniesUsersResponseDto2> companyResponseMap = new HashMap<>();

		for (UserRole ur : userRoles) {

			User user = userMap.get(ur.getUserId());
			CompanyBasicResponse company = companyMap.get(ur.getCompanyId());

			if (user == null || company == null) {
				continue;
			}

			// ----- COMPANY -----
			RoleCompaniesUsersResponseDto2 companyResp = companyResponseMap.get(company.getCompanyId());
			if (companyResp == null) {
				companyResp = new RoleCompaniesUsersResponseDto2();
				companyResp.setCompanyId(company.getCompanyId());
				companyResp.setCompanyCode(company.getCompanyCode());
				companyResp.setCompanyName(company.getCompanyName());
				companyResp.setActive(company.getActive());
				companyResponseMap.put(company.getCompanyId(), companyResp);
			}

			// ----- USER -----
			RoleCompaniesUsersResponseDto3 userResp = new RoleCompaniesUsersResponseDto3();
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
	public ApiResponse<UserCompaniesRolesResponseDto1> getUserRolesByUserId(UUID userId) {

		User user = userRepository.findByIdAndActiveTrueAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// ---------- USER ----------
		UserCompaniesRolesResponseDto1 response = new UserCompaniesRolesResponseDto1();
		response.setUserId(user.getId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setMobileNumber(user.getMobileNumber());
		response.setActive(user.getActive());

		List<UserRole> userRoles = userRoleRepository.findByUserIdAndDeletedAtIsNull(userId);
		if (userRoles.isEmpty()) {
			return new ApiResponse<>(true, "User has no roles", HttpStatus.OK.name(), HttpStatus.OK.value(), response);
		}

		// ---------- COLLECT IDS ----------
		List<UUID> roleIds = new ArrayList<>();
		List<UUID> companyIds = new ArrayList<>();

		for (UserRole ur : userRoles) {
			roleIds.add(ur.getRoleId());
			companyIds.add(ur.getCompanyId());
		}

		// ---------- ROLES ----------
		List<Role> roles = roleRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(roleIds);
		Map<UUID, Role> roleMap = new HashMap<>();
		for (Role r : roles) {
			roleMap.put(r.getId(), r);
		}

		// ---------- COMPANIES ----------
		CompanyIdsRequest cir = new CompanyIdsRequest();
		cir.setCompanyIds(companyIds);

		List<CompanyBasicResponse> companies;
		try {
			companies = orgServiceClient.getCompanyBasicByCompanyIds(cir).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			throw new OrgServiceException("Error while communicating with Organization Service", status, e);
		}

		Map<UUID, UserCompaniesRolesResponseDto2> companyMap = new HashMap<>();
		for (CompanyBasicResponse c : companies) {
			UserCompaniesRolesResponseDto2 crd = new UserCompaniesRolesResponseDto2();
			crd.setCompanyId(c.getCompanyId());
			crd.setCompanyCode(c.getCompanyCode());
			crd.setCompanyName(c.getCompanyName());
			crd.setActive(c.getActive());
			companyMap.put(c.getCompanyId(), crd);
		}

		// ---------- BUILD COMPANY → ROLES ----------
		for (UserRole ur : userRoles) {

			Role role = roleMap.get(ur.getRoleId());
			UserCompaniesRolesResponseDto2 company = companyMap.get(ur.getCompanyId());

			if (role == null || company == null) {
				continue;
			}

			UserCompaniesRolesResponseDto3 rrd = new UserCompaniesRolesResponseDto3();
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
	public ApiResponse<UserCompanyRolesResponseDto1> getUserRolesByUserIdAndCompanyId(UUID userId, UUID companyId) {

	    // 🔹 Validate User
	    User user = userRepository.findByIdAndActiveTrueAndDeletedAtIsNull(userId)
	            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

	    // 🔹 Validate Company via org service
	    CompanyBasicResponse companyBasic;
	    try {
	        ResponseEntity<ApiResponse<CompanyBasicResponse>> companyResponse = orgServiceClient.getCompanyBasic(companyId);
	        companyBasic = companyResponse.getBody().getData();
	    } catch (FeignException e) {
	        HttpStatus status = HttpStatus.resolve(e.status());
	        String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!" 
	                : "Error while communicating with Organization Service";
	        throw new OrgServiceException(message, status, e);
	    }

	    // 🔹 Fetch User Roles for this company
	    List<UserRole> userRoles = userRoleRepository.findByUserIdAndCompanyIdAndDeletedAtIsNull(userId, companyId);
	    List<UUID> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();

	    List<Role> roles = roleRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(roleIds);
	    Map<UUID, Role> roleMap = roles.stream().collect(Collectors.toMap(Role::getId, r -> r));

	    // ---------- BUILD RESPONSE ----------
	    UserCompanyRolesResponseDto1 response = new UserCompanyRolesResponseDto1();
	    response.setUserId(user.getId());
	    response.setName(user.getName());
	    response.setEmail(user.getEmail());
	    response.setMobileNumber(user.getMobileNumber());
	    response.setActive(user.getActive());

	    // Company info
	    UserCompanyRolesResponseDto2 companyDto = new UserCompanyRolesResponseDto2();
	    companyDto.setCompanyId(companyBasic.getCompanyId());
	    companyDto.setCompanyCode(companyBasic.getCompanyCode());
	    companyDto.setCompanyName(companyBasic.getCompanyName());
	    companyDto.setActive(companyBasic.getActive());

	    // Roles assigned to this user in this company
	    for (UserRole ur : userRoles) {
	        Role role = roleMap.get(ur.getRoleId());
	        if (role == null) continue;

	        UserCompanyRolesResponseDto3 roleDto = new UserCompanyRolesResponseDto3();
	        roleDto.setRoleId(role.getId());
	        roleDto.setRoleCode(role.getRoleCode());
	        roleDto.setRoleName(role.getRoleName());
	        roleDto.setRoleIsActive(role.getActive());
	        roleDto.setUserRoleId(ur.getId());
	        roleDto.setAssignedIsActive(ur.getActive());
	        roleDto.setAssignedAt(ur.getCreatedAt());
	        roleDto.setRevokedAt(ur.getDeletedAt());

	        companyDto.getRoles().add(roleDto);
	    }

	    // 🔹 Attach company to user
	    response.setCompany(companyDto);

	    String message = userRoles.isEmpty() ? "User has no roles for this company" : "User roles fetched successfully";

	    return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), response);
	}


}
