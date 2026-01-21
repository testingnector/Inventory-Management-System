package com.nector.auth.dto.response.role_permission;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolePermissionResponse {

//	-------PERMISSION RESPONSE----------
	private UUID permissionId;
	private String permissionCode;
	private String permissionName;
	private String permissionDescription;
	private String moduleName;
	private Boolean permissionActive;

//	-------ROLE_PERMISSION RESPONSE----------
	private Boolean allowed;
	private Boolean rolePermissionActive;
	private LocalDateTime assignedAt;
}
