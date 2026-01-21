package com.nector.auth.dto.response.role_permission;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class PermissionRoleResponse {

	private UUID roleId;
    private String roleCode;
    private String roleName;
    private String roleDescription;
    private Boolean systemRole;
    private Boolean roleIsActive;
    
	private Boolean allowed;
	private Boolean assignedActive;
	private LocalDateTime assignedAt;
}
