package com.nector.auth.security.etc;

import java.net.http.HttpHeaders;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.nector.auth.security.jwt.JwtTokenProvider;

@Component
public class etc {

//    private final JwtTokenProvider jwtTokenProvider;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//
//        String authHeader = exchange.getRequest()
//                .getHeaders()
//                .getFirst(HttpHeaders.AUTHORIZATION);
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return chain.filter(exchange);
//        }
//
//        String token = authHeader.substring(7);
//
//        String userId = jwtUtil.getUserId(token);
//        String role = jwtUtil.getRole(token);
//        String companyId = jwtUtil.getCompanyId(token);
//
//        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
//                .header("X-USER-ID", userId)
//                .header("X-USER-ROLE", role)
//                .header("X-COMPANY-ID", companyId)
//                .build();
//
//        return chain.filter(exchange.mutate().request(mutatedRequest).build());
//    }
	
	
	
//	@Override
//	@Transactional
//	public ApiResponse<List<UserPermissionGroupResponse>> getAllUserPermissions() {
//
//	    // 1️⃣ fetch all active users
//	    List<User> users = userRepository.findByDeletedAtIsNull();
//	    if (users == null || users.isEmpty()) {
//	        throw new ResourceNotFoundException("No users found");
//	    }
//
//	    // 2️⃣ fetch all active user permissions
//	    List<UserPermission> userPermissions = userPermissionRepository.findByDeletedAtIsNull();
//
//	    // 3️⃣ fetch all active user roles
//	    List<UserRole> userRoles = userRoleRepository.findByUserIdInAndActiveTrueAndDeletedAtIsNull(
//	            getUserIds(users)
//	    );
//
//	    // 4️⃣ fetch all active role permissions
//	    List<UUID> roleIds = getRoleIds(userRoles);
//	    List<RolePermission> rolePermissions = roleIds.isEmpty() ? new ArrayList<RolePermission>() :
//	            rolePermissionRepository.findByRoleIdInAndActiveTrueAndDeletedAtIsNull(roleIds);
//
//	    // 5️⃣ fetch permission master data
//	    List<UUID> permissionIds = getPermissionIds(rolePermissions, userPermissions);
//	    List<Permission> permissions = permissionRepository.findByIdInAndDeletedAtIsNull(permissionIds);
//	    Map<UUID, Permission> permissionMap = new HashMap<UUID, Permission>();
//	    for (Permission p : permissions) {
//	        permissionMap.put(p.getId(), p);
//	    }
//
//	    // 6️⃣ group userRoles → rolePermissions
//	    Map<UUID, List<RolePermission>> rolePermissionMap = new HashMap<UUID, List<RolePermission>>();
//	    for (RolePermission rp : rolePermissions) {
//	        List<RolePermission> list = rolePermissionMap.get(rp.getRoleId());
//	        if (list == null) {
//	            list = new ArrayList<RolePermission>();
//	            rolePermissionMap.put(rp.getRoleId(), list);
//	        }
//	        list.add(rp);
//	    }
//
//	    // 7️⃣ group userRoles → map of userId → List<roleId>
//	    Map<UUID, List<UUID>> userRoleMap = new HashMap<UUID, List<UUID>>();
//	    for (UserRole ur : userRoles) {
//	        List<UUID> list = userRoleMap.get(ur.getUserId());
//	        if (list == null) {
//	            list = new ArrayList<UUID>();
//	            userRoleMap.put(ur.getUserId(), list);
//	        }
//	        list.add(ur.getRoleId());
//	    }
//
//	    // 8️⃣ flat map of user-permissions (for grouping)
//	    Map<String, UserPermissionResponse> flatMap = new HashMap<String, UserPermissionResponse>();
//
//	    for (User user : users) {
//	        // ROLE default permissions
//	        List<UUID> roles = userRoleMap.get(user.getId());
//	        if (roles != null) {
//	            for (UUID roleId : roles) {
//	                List<RolePermission> rps = rolePermissionMap.get(roleId);
//	                if (rps != null) {
//	                    for (RolePermission rp : rps) {
//	                        Permission perm = permissionMap.get(rp.getPermissionId());
//	                        if (perm == null) continue;
//
//	                        UserPermissionResponse upr = new UserPermissionResponse();
//	                        upr.setUserId(user.getId());
//	                        upr.setName(user.getName());
//	                        upr.setEmail(user.getEmail());
//	                        upr.setMobileNumber(user.getMobileNumber());
//	                        upr.setPermissionId(perm.getId());
//	                        upr.setPermissionCode(perm.getPermissionCode());
//	                        upr.setPermissionName(perm.getPermissionName());
//	                        upr.setDescription(perm.getDescription());
//	                        upr.setModuleName(perm.getModuleName());
//	                        upr.setAllowed(true);
//	                        upr.setActive(true);
//	                        upr.setAssignedAt(rp.getAssignedAt());
//
//	                        String key = user.getId() + ":" + perm.getId();
//	                        flatMap.put(key, upr);
//	                    }
//	                }
//	            }
//	        }
//	    }
//
//	    // USER overrides
//	    for (UserPermission up : userPermissions) {
//	        Permission perm = permissionMap.get(up.getPermissionId());
//	        if (perm == null) continue;
//
//	        User user = findUserById(users, up.getUserId());
//	        if (user == null) continue;
//
//	        UserPermissionResponse upr = new UserPermissionResponse();
//	        upr.setUserId(user.getId());
//	        upr.setName(user.getName());
//	        upr.setEmail(user.getEmail());
//	        upr.setMobileNumber(user.getMobileNumber());
//	        upr.setPermissionId(perm.getId());
//	        upr.setPermissionCode(perm.getPermissionCode());
//	        upr.setPermissionName(perm.getPermissionName());
//	        upr.setDescription(perm.getDescription());
//	        upr.setModuleName(perm.getModuleName());
//	        upr.setAllowed(up.getAllowed());
//	        upr.setActive(up.getActive());
//	        upr.setAssignedAt(up.getAssignedAt());
//
//	        String key = user.getId() + ":" + perm.getId();
//	        flatMap.put(key, upr); // override
//	    }
//
//	    // 9️⃣ group by user
//	    Map<UUID, UserPermissionGroupResponse> userMap = new HashMap<UUID, UserPermissionGroupResponse>();
//	    for (UserPermissionResponse upr : flatMap.values()) {
//	        UserPermissionGroupResponse ugr = userMap.get(upr.getUserId());
//	        if (ugr == null) {
//	            ugr = new UserPermissionGroupResponse();
//	            ugr.setUserId(upr.getUserId());
//	            ugr.setName(upr.getName());
//	            ugr.setEmail(upr.getEmail());
//	            ugr.setMobileNumber(upr.getMobileNumber());
//	            userMap.put(ugr.getUserId(), ugr);
//	        }
//
//	        PermissionResponse pr = new PermissionResponse();
//	        pr.setPermissionId(upr.getPermissionId());
//	        pr.setPermissionCode(upr.getPermissionCode());
//	        pr.setPermissionName(upr.getPermissionName());
//	        pr.setDescription(upr.getDescription());
//	        pr.setModuleName(upr.getModuleName());
//	        pr.setAllowed(upr.getAllowed());
//	        pr.setActive(upr.getActive());
//	        pr.setAssignedAt(upr.getAssignedAt());
//
//	        ugr.getPermissions().add(pr);
//	    }
//
//	    if (userMap.isEmpty()) {
//	        throw new ResourceNotFoundException("No User-Permission mapping found");
//	    }
//
//	    return new ApiResponse<List<UserPermissionGroupResponse>>(
//	            true,
//	            "User Permission fetch successfully",
//	            HttpStatus.OK.name(),
//	            HttpStatus.OK.value(),
//	            new ArrayList<UserPermissionGroupResponse>(userMap.values())
//	    );
//	}
//               
//	
//	private List<UUID> getUserIds(List<User> users) {
//	    List<UUID> ids = new ArrayList<UUID>();
//	    for (User u : users) ids.add(u.getId());
//	    return ids;
//	}
//
//	private List<UUID> getRoleIds(List<UserRole> userRoles) {
//	    List<UUID> ids = new ArrayList<UUID>();
//	    for (UserRole ur : userRoles) ids.add(ur.getRoleId());
//	    return ids;
//	}
//
//	private List<UUID> getPermissionIds(List<RolePermission> rolePermissions, List<UserPermission> userPermissions) {
//	    List<UUID> ids = new ArrayList<UUID>();
//	    for (RolePermission rp : rolePermissions) {
//	        if (!ids.contains(rp.getPermissionId())) ids.add(rp.getPermissionId());
//	    }
//	    for (UserPermission up : userPermissions) {
//	        if (!ids.contains(up.getPermissionId())) ids.add(up.getPermissionId());
//	    }
//	    return ids;
//	}
//
//	private User findUserById(List<User> users, UUID id) {
//	    for (User u : users) {
//	        if (u.getId().equals(id)) return u;
//	    }
//	    return null;
//	}

	
	
	
	
	
	
	
	
}
